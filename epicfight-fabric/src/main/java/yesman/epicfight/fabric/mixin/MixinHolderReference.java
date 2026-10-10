package yesman.epicfight.fabric.mixin;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/// NeoForge makes reference holders equal to a `DeferredHolder` with the same key (and hash by
/// key), so both can be used interchangeably as map keys (attribute maps, mob effect maps...).
/// The `DeferredHolder` shim relies on the same contract.
@Mixin(Holder.Reference.class)
public abstract class MixinHolderReference<T> {
    @Shadow
    @Nullable
    private ResourceKey<T> key;

    @Override
    public int hashCode() {
        return this.key != null ? this.key.hashCode() : System.identityHashCode(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        return this.key != null && obj instanceof Holder<?> holder && holder.kind() == Holder.Kind.REFERENCE && holder.unwrapKey().map(this.key::equals).orElse(false);
    }
}
