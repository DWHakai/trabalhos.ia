"""Checks mixin injector targets against the bytecode of the Minecraft jar mapped by Loom.

For every injector, the target method must exist (with the descriptor when one is given) and
each INVOKE/FIELD target must appear in its bytecode with the same owner and descriptor, at
least ordinal + 1 times. Mixin only checks this when the class loads, so this catches targets
that only exist in NeoForge's patched code without starting the game.

usage: python tools/check_mixins.py <javap> <minecraft-merged jar from .gradle/loom-cache> <mixin src dir>...
"""
import re, os, sys, subprocess

JAVAP, JAR = sys.argv[1], sys.argv[2]
DIRS = sys.argv[3:]
cache = {}

def javap(cls):
    if cls not in cache:
        out = subprocess.run([JAVAP, '-c', '-p', '-s', '-cp', JAR, cls], capture_output=True, text=True).stdout
        methods = {}
        cur = None
        for line in out.splitlines():
            m = re.match(r'  (?:[\w<>\[\]$.,? ]+ )?([\w$<>.]+)\((.*)\)(?: throws [\w.$, ]+)?;$', line)
            if m and not line.startswith('    ') and '.' in m.group(1):
                cur = ['<init>', None, []]
                methods.setdefault('<init>', []).append(cur)
                continue
            if m and not line.startswith('    '):
                cur = [m.group(1), None, []]
                methods.setdefault(m.group(1), []).append(cur)
                continue
            if m is None and re.match(r'  [\w.$]+\(.*\);$', line):  # constructor
                cur = ['<init>', None, []]
                methods.setdefault('<init>', []).append(cur)
                continue
            d = re.match(r'    descriptor: (.*)', line)
            if d and cur is not None and cur[1] is None:
                cur[1] = d.group(1)
                continue
            if cur is not None and re.match(r'\s+\d+: ', line):
                cur[2].append(line)
        cache[cls] = (out, methods)
    return cache[cls]

def resolve(simple, s, own_pkg):
    imports = dict((m.group(2), m.group(1) + '.' + m.group(2)) for m in re.finditer(r'import ([\w.]+)\.(\w+);', s))
    head = simple.split('.')[0]
    if head in imports:
        base = imports[head]
        rest = simple.split('.')[1:]
        return base + ''.join('$' + r for r in rest)
    # nested import like RenderType.CompositeRenderType imported directly
    for m in re.finditer(r'import ([\w.]+)\.\*;', s):
        cand = m.group(1) + '.' + simple
        if javap(cand)[1]:
            return cand
    return simple

def norm_owner(owner, cls):
    return owner.replace('/', '.')

problems = 0
for d in DIRS:
    for root, _, files in os.walk(d):
        for f in sorted(files):
            if not f.endswith('.java'): continue
            s = open(os.path.join(root, f), encoding='utf-8').read()
            s = re.sub(r'/\*.*?\*/', '', s, flags=re.S)
            mm = re.search(r'@Mixin\(\s*(?:value\s*=\s*)?\{?\s*([\w.]+)\.class', s)
            if not mm: continue
            cls = resolve(mm.group(1), s, None)
            # nested class imported as Outer.Inner
            imp = re.search(r'import ([\w.]+)\.' + re.escape(mm.group(1)) + ';', s)
            if imp and imp.group(1)[0].islower() is False:
                pass
            out, methods = javap(cls)
            if not methods:
                # maybe a nested class imported by its full dotted name
                parts = cls.split('.')
                for k in range(len(parts) - 1, 0, -1):
                    cand = '.'.join(parts[:k]) + '$' + '$'.join(parts[k:])
                    out, methods = javap(cand)
                    if methods:
                        cls = cand
                        break
            if not methods:
                print(f'{f}: class {cls} not in jar'); problems += 1; continue
            for am in re.finditer(r'@(Inject|WrapOperation|ModifyVariable|ModifyArg|ModifyArgs|Redirect|ModifyExpressionValue|WrapWithCondition|ModifyReturnValue|ModifyConstant)\s*\((.*?)\)\s*(?:private|public|protected)', s, re.S):
                ann = am.group(2)
                mlist = re.findall(r'method\s*=\s*(\{[^}]*\}|"[^"]*")', ann)
                mnames = re.findall(r'"([^"]+)"', mlist[0]) if mlist else []
                ats = re.findall(r'@At\s*\((.*?)\)(?=\s*[,)}\]])', ann, re.S)
                for mn in mnames:
                    name = mn.split('(')[0]
                    desc = mn[len(name):] or None
                    cands = methods.get(name, [])
                    if desc:
                        cands = [c for c in cands if c[1] == desc]
                    if not cands:
                        print(f'{f}: method {mn} not in {cls}'); problems += 1; continue
                    # @ModifyConstant(constant = @Constant(intValue = N)): javac folds only real
                    # constants, so `10 + font.lineHeight` has a 10, never the sum
                    for iv in re.findall(r'@Constant\s*\(\s*intValue\s*=\s*(-?\d+)', ann):
                        n = int(iv)
                        pat = {-1: r'iconst_m1$', **{k: rf'iconst_{k}$' for k in range(6)}}.get(n, rf'(?:bipush|sipush|ldc(?:_w)?)\s+(?:#\d+\s+// int )?{n}$')
                        for c in cands:
                            count = sum(1 for line in c[2] if re.search(pat, line))
                            if count == 0:
                                print(f'{f}: {name}{c[1]} has no int constant {n}'); problems += 1
                    # CONSTANT classValue (instanceof/checkcast/class literal): one of the @At entries
                    # has to match (the others may be intermediary names for the built jar)
                    class_values = [re.search(r'classValue=([\w/$]+)', at).group(1) for at in ats if re.search(r'"CONSTANT"', at) and re.search(r'classValue=', at)]
                    if class_values:
                        if am.group(1) == 'ModifyExpressionValue':
                            print(f'{f}: @ModifyExpressionValue on a classValue gets the Class, not the instanceof result (use @WrapOperation)'); problems += 1
                        for c in cands:
                            if not any(re.search(r'(?:instanceof|checkcast|ldc(?:_w)?)\s+#\d+\s+// class ' + re.escape(cv) + r'$', line) for cv in class_values for line in c[2]):
                                print(f'{f}: {name}{c[1]} has none of the classes {class_values}'); problems += 1
                    for at in ats:
                        value = re.search(r'value\s*=\s*"(\w+)"', at) or re.match(r'\s*"(\w+)"', at)
                        value = value.group(1) if value else None
                        tm = re.search(r'target\s*=\s*"([^"]+)"', at)
                        if value not in ('INVOKE', 'FIELD', 'INVOKE_ASSIGN') or not tm:
                            continue
                        target = tm.group(1)
                        om = re.match(r'L([\w/$]+);([\w$<>]+)(:?)(.*)', target)
                        if not om:
                            print(f'{f}: unqualified target {target} in {mn}'); continue
                        owner, tname, colon, tdesc = om.groups()
                        ordm = re.search(r'ordinal\s*=\s*(\d+)', at)
                        ordinal = int(ordm.group(1)) if ordm else None
                        kind = 'Field' if value == 'FIELD' else '(?:Interface)?Method'
                        own = owner.replace('/', '.')
                        if own == cls.replace('$', '$'):
                            pat = r'// ' + kind + r' (?:' + re.escape(owner) + r'\.)?' + re.escape(tname) + ':' + re.escape(tdesc) + r'$'
                        else:
                            pat = r'// ' + kind + r' ' + re.escape(owner) + r'\.' + re.escape(tname) + ':' + re.escape(tdesc) + r'$'
                        for c in cands:
                            count = sum(1 for line in c[2] if re.search(pat, line))
                            if count == 0:
                                print(f'{f}: {name}{c[1]} has no {target}'); problems += 1
                            elif ordinal is not None and ordinal >= count:
                                print(f'{f}: {name}{c[1]} has {count} x {target}, ordinal {ordinal}'); problems += 1
                            elif ordinal is None and count > 1 and value != 'FIELD':
                                print(f'  note {f}: {name} matches {count} x {tname} (no ordinal)')
print('problems:', problems)
