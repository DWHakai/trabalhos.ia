package yesman.epicfight.platform.registry.callback;

import net.minecraft.core.Registry;

/// Same contract as NeoForge's `ClearCallback`: runs before the registry ids are remapped.
@FunctionalInterface
public interface ClearCallback<T> extends RegistryCallback<T> {
    void onClear(Registry<T> registry, boolean full);
}
