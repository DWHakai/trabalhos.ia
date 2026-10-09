package yesman.epicfight.platform.registry.callback;

import net.minecraft.core.Registry;

/// Same contract as NeoForge's `BakeCallback`: runs once every entry is registered, and again after
/// the registry ids are remapped (e.g., when joining a server).
@FunctionalInterface
public interface BakeCallback<T> extends RegistryCallback<T> {
    void onBake(Registry<T> registry);
}
