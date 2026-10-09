#!/usr/bin/env python3
"""Converts the upstream NeoForge access transformer into a Fabric access widener.

NeoForge 1.21.1 access transformers already use Mojang names, which is also what
this project uses at dev time (Loom with officialMojangMappings), so only the
format changes. Access transformers do not carry field descriptors and access
wideners need them, so the descriptors are read from the Mojang-mapped Minecraft
jar that Loom produces.

Usage:
    python3 tools/at2aw.py upstream-meta/accesstransformer.cfg <mapped-minecraft.jar> \
        > src/main/resources/epicfight.accesswidener

    The mapped jar is under ~/.gradle/caches/fabric-loom/minecraftMaven/net/minecraft/
    (minecraft-merged-*.jar whose path contains "layered"). Without a jar, field
    entries are written as comments ("bootstrap" mode) so Loom can still set up
    the workspace and produce that jar.
"""

import struct
import sys
import zipfile


def read_fields(class_bytes):
    """Returns {field_name: descriptor} for a class file."""
    pos = 8  # magic + minor + major
    (cp_count,) = struct.unpack_from(">H", class_bytes, pos)
    pos += 2
    utf8 = {}
    i = 1
    while i < cp_count:
        tag = class_bytes[pos]
        pos += 1
        if tag == 1:
            (length,) = struct.unpack_from(">H", class_bytes, pos)
            pos += 2
            utf8[i] = class_bytes[pos:pos + length].decode("utf-8", "replace")
            pos += length
        elif tag in (3, 4, 9, 10, 11, 12, 17, 18):
            pos += 4
        elif tag in (5, 6):
            pos += 8
            i += 1  # long/double take two slots
        elif tag in (7, 8, 16, 19, 20):
            pos += 2
        elif tag == 15:
            pos += 3
        else:
            raise ValueError(f"unknown constant pool tag {tag}")
        i += 1
    pos += 6  # access, this, super
    (interfaces,) = struct.unpack_from(">H", class_bytes, pos)
    pos += 2 + 2 * interfaces
    (field_count,) = struct.unpack_from(">H", class_bytes, pos)
    pos += 2
    fields = {}
    for _ in range(field_count):
        _, name_idx, desc_idx, attr_count = struct.unpack_from(">HHHH", class_bytes, pos)
        pos += 8
        for _ in range(attr_count):
            (attr_len,) = struct.unpack_from(">I", class_bytes, pos + 2)
            pos += 6 + attr_len
        fields[utf8[name_idx]] = utf8[desc_idx]
    return fields


def convert(at_lines, jar):
    out = ["accessWidener v2 named", ""]
    missing = 0
    field_cache = {}

    def field_desc(owner, name):
        if jar is None:
            return None
        if owner not in field_cache:
            try:
                field_cache[owner] = read_fields(jar.read(owner + ".class"))
            except KeyError:
                field_cache[owner] = {}
        return field_cache[owner].get(name)

    for raw in at_lines:
        line = raw.split("#", 1)[0].strip()
        if not line:
            continue
        parts = line.split()
        modifier, owner = parts[0], parts[1].replace(".", "/")
        widen = "extendable" if modifier.startswith("protected") else "accessible"
        remove_final = modifier.endswith("-f")

        if len(parts) == 2:  # class
            out.append(f"{widen} class {owner}")
            if remove_final and widen != "extendable":
                out.append(f"extendable class {owner}")
            continue

        member = parts[2]
        if "(" in member:  # method or constructor
            name, desc = member[:member.index("(")], member[member.index("("):]
            out.append(f"{widen} method {owner} {name} {desc}")
            if remove_final and widen != "extendable":
                out.append(f"extendable method {owner} {name} {desc}")
            continue

        # field: protected has no access widener equivalent, accessible is the closest
        desc = field_desc(owner, member)
        if desc is None:
            missing += 1
            out.append(f"# TODO descriptor: accessible field {owner} {member} ?")
            if remove_final:
                out.append(f"# TODO descriptor: mutable field {owner} {member} ?")
            continue
        out.append(f"accessible field {owner} {member} {desc}")
        if remove_final:
            out.append(f"mutable field {owner} {member} {desc}")

    return out, missing


def main():
    if len(sys.argv) not in (2, 3):
        sys.exit(__doc__)
    with open(sys.argv[1], encoding="utf-8") as f:
        at_lines = f.readlines()
    jar = zipfile.ZipFile(sys.argv[2]) if len(sys.argv) == 3 else None
    out, missing = convert(at_lines, jar)
    print("\n".join(out))
    if missing:
        print(f"at2aw: {missing} field(s) without descriptor, left as TODO comments", file=sys.stderr)


if __name__ == "__main__":
    main()
