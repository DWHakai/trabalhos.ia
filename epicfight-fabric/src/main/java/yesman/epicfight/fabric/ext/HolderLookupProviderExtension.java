package yesman.epicfight.fabric.ext;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;

import java.util.Optional;

/// NeoForge's `IHolderLookupProviderExtension`, injected into `HolderLookup.Provider`.
public interface HolderLookupProviderExtension {
    default <T> Holder<T> holderOrThrow(ResourceKey<T> key) {
        return ((HolderLookup.Provider)this).lookupOrThrow(key.registryKey()).getOrThrow(key);
    }

    default <T> Optional<Holder.Reference<T>> holder(ResourceKey<T> key) {
        return ((HolderLookup.Provider)this).lookupOrThrow(key.registryKey()).get(key);
    }
}
