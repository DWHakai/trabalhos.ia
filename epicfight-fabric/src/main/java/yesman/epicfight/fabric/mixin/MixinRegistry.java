package yesman.epicfight.fabric.mixin;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import yesman.epicfight.platform.registry.DeferredHolder;

/// NeoForge patch of `Registry#safeCastToReference`: it casts `holder.getDelegate()`, so a
/// [DeferredHolder] (not a `Holder.Reference`) can be written by `holderByNameCodec`, for
/// example the skill of a skill book or an Epic Fight attribute of an item.
@Mixin(Registry.class)
public interface MixinRegistry<T> {
    @ModifyVariable(method = "safeCastToReference", at = @At("HEAD"), argsOnly = true)
    private Holder<T> epicfight$unwrapDeferredHolder(Holder<T> holder) {
        return holder instanceof DeferredHolder<T, ?> deferredHolder && deferredHolder.isBound() ? deferredHolder.getDelegate() : holder;
    }
}
