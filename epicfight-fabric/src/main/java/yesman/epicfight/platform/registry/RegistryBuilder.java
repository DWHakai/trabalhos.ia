package yesman.epicfight.platform.registry;

import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.fabricmc.fabric.api.event.registry.RegistryIdRemapCallback;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import yesman.epicfight.platform.registry.callback.AddCallback;
import yesman.epicfight.platform.registry.callback.BakeCallback;
import yesman.epicfight.platform.registry.callback.ClearCallback;
import yesman.epicfight.platform.registry.callback.RegistryCallback;

import java.util.ArrayList;
import java.util.List;

/// Fabric replacement for NeoForge's `RegistryBuilder`, keeping the same fluent API.
///
/// The registry is created and added to the root registry right away by [#create()],
/// so there is no `NewRegistryEvent` step on Fabric.
public class RegistryBuilder<T> {
    private static final List<Runnable> BAKE_TASKS = new ArrayList<>();

    private final ResourceKey<Registry<T>> key;
    private final List<RegistryCallback<T>> callbacks = new ArrayList<>();
    private boolean sync;

    public RegistryBuilder(ResourceKey<Registry<T>> key) {
        this.key = key;
    }

    public RegistryBuilder<T> callback(RegistryCallback<T> callback) {
        this.callbacks.add(callback);
        return this;
    }

    public RegistryBuilder<T> sync(boolean sync) {
        this.sync = sync;
        return this;
    }

    public Registry<T> create() {
        FabricRegistryBuilder<T, MappedRegistry<T>> builder = FabricRegistryBuilder.createSimple(this.key);

        if (this.sync) {
            builder.attribute(RegistryAttribute.SYNCED);
        }

        MappedRegistry<T> registry = builder.buildAndRegister();

        for (RegistryCallback<T> callback : this.callbacks) {
            if (callback instanceof AddCallback<T> add) {
                RegistryEntryAddedCallback.event(registry).register((rawId, id, value) ->
                    add.onAdd(registry, rawId, ResourceKey.create(this.key, id), value));
            }

            if (callback instanceof ClearCallback<T> clear) {
                RegistryIdRemapCallback.event(registry).register(state -> clear.onClear(registry, false));
            }

            if (callback instanceof BakeCallback<T> bake) {
                BAKE_TASKS.add(() -> bake.onBake(registry));
                RegistryIdRemapCallback.event(registry).register(state -> bake.onBake(registry));
            }
        }

        return registry;
    }

    /// Runs the bake callbacks. Called once every [DeferredRegister] has registered its entries.
    public static void bakeAll() {
        BAKE_TASKS.forEach(Runnable::run);
    }
}
