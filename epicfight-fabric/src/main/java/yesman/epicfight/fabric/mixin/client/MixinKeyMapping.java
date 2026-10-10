package yesman.epicfight.fabric.mixin.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.fabric.ext.KeyMappingExtension;
import yesman.epicfight.platform.client.settings.IKeyConflictContext;
import yesman.epicfight.platform.client.settings.KeyConflictContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/// NeoForge's key mapping changes (`KeyMappingLookup` + conflict contexts), without key modifiers
/// (Epic Fight does not use them):
///
/// - Vanilla keeps one mapping per key, so only the last one registered for a key ever fires.
///   Epic Fight binds attack/innate skill to the left button, guard to the right button and the
///   mover skill to space, on top of the vanilla mappings, so every mapping of a key must fire
///   (`click`/`set` below), as NeoForge does.
/// - A mapping is only down while its conflict context is active (`isDown`).
@Mixin(KeyMapping.class)
public abstract class MixinKeyMapping implements KeyMappingExtension {
    @Shadow @Final private static Map<String, KeyMapping> ALL;
    @Shadow private InputConstants.Key key;
    @Shadow private int clickCount;

    @Shadow public abstract void setDown(boolean value);

    @Unique
    private static final Map<InputConstants.Key, Collection<KeyMapping>> epicfight$LOOKUP = new ConcurrentHashMap<>();

    @Unique
    private IKeyConflictContext epicfight$keyConflictContext = KeyConflictContext.UNIVERSAL;

    @Unique
    private static List<KeyMapping> epicfight$getAll(InputConstants.Key key) {
        Collection<KeyMapping> mappings = epicfight$LOOKUP.get(key);

        if (mappings == null) {
            return List.of();
        }

        List<KeyMapping> matching = new ArrayList<>(mappings.size());

        for (KeyMapping mapping : mappings) {
            if (mapping.isActiveAndMatches(key)) {
                matching.add(mapping);
            }
        }

        return matching;
    }

    @Unique
    private static void epicfight$put(InputConstants.Key key, KeyMapping mapping) {
        epicfight$LOOKUP.computeIfAbsent(key, k -> new CopyOnWriteArrayList<>()).add(mapping);
    }

    @Inject(method = "<init>(Ljava/lang/String;Lcom/mojang/blaze3d/platform/InputConstants$Type;ILjava/lang/String;)V", at = @At("TAIL"))
    private void epicfight$register(CallbackInfo ci) {
        epicfight$put(this.key, (KeyMapping)(Object)this);
    }

    @Inject(method = "resetMapping", at = @At("TAIL"))
    private static void epicfight$resetMapping(CallbackInfo ci) {
        epicfight$LOOKUP.clear();

        for (KeyMapping mapping : ALL.values()) {
            epicfight$put(mapping.getKey(), mapping);
        }
    }

    @Inject(method = "click", at = @At("HEAD"), cancellable = true)
    private static void epicfight$click(InputConstants.Key key, CallbackInfo ci) {
        for (KeyMapping mapping : epicfight$getAll(key)) {
            ((MixinKeyMapping)(Object)mapping).clickCount++;
        }

        ci.cancel();
    }

    @Inject(method = "set", at = @At("HEAD"), cancellable = true)
    private static void epicfight$set(InputConstants.Key key, boolean held, CallbackInfo ci) {
        for (KeyMapping mapping : epicfight$getAll(key)) {
            mapping.setDown(held);
        }

        ci.cancel();
    }

    @Inject(method = "isDown", at = @At("RETURN"), cancellable = true)
    private void epicfight$isDown(CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ() && !this.isConflictContextAndModifierActive()) {
            cir.setReturnValue(false);
        }
    }

    @Override
    public InputConstants.Key getKey() {
        return this.key;
    }

    @Override
    public IKeyConflictContext getKeyConflictContext() {
        return this.epicfight$keyConflictContext;
    }

    @Override
    public void setKeyConflictContext(IKeyConflictContext keyConflictContext) {
        this.epicfight$keyConflictContext = keyConflictContext;
    }
}
