package yesman.epicfight.platform.registry.callback;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

/// Same contract as NeoForge's `AddCallback`, wired to Fabric by [yesman.epicfight.platform.registry.RegistryBuilder].
@FunctionalInterface
public interface AddCallback<T> extends RegistryCallback<T> {
    void onAdd(Registry<T> registry, int id, ResourceKey<T> key, T value);
}
