package yesman.epicfight.platform.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/// Fabric replacement for NeoForge's `DeferredRegister`, keeping the API the upstream code uses.
///
/// Entries are queued by [#register] and added to the registry by [#registerAll()], which
/// replaces NeoForge's `register(IEventBus)` and has to run during mod initialization.
public class DeferredRegister<T> {
    private final ResourceKey<? extends Registry<T>> registryKey;
    private final String namespace;
    private final Map<DeferredHolder<T, ? extends T>, Supplier<? extends T>> entries = new LinkedHashMap<>();
    private final Collection<DeferredHolder<T, ? extends T>> entriesView = Collections.unmodifiableSet(this.entries.keySet());
    private boolean registered;

    protected DeferredRegister(ResourceKey<? extends Registry<T>> registryKey, String namespace) {
        this.registryKey = Objects.requireNonNull(registryKey);
        this.namespace = Objects.requireNonNull(namespace);
    }

    public static <T> DeferredRegister<T> create(ResourceKey<? extends Registry<T>> key, String namespace) {
        return new DeferredRegister<>(key, namespace);
    }

    public static <T> DeferredRegister<T> create(Registry<T> registry, String namespace) {
        return new DeferredRegister<>(registry.key(), namespace);
    }

    public <I extends T> DeferredHolder<T, I> register(String name, Supplier<? extends I> sup) {
        return this.register(name, id -> sup.get());
    }

    public <I extends T> DeferredHolder<T, I> register(String name, Function<ResourceLocation, ? extends I> func) {
        if (this.registered) {
            throw new IllegalStateException("Cannot register new entries to DeferredRegister after registerAll() has been called.");
        }

        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(this.namespace, name);
        DeferredHolder<T, I> holder = DeferredHolder.create(ResourceKey.create(this.registryKey, id));

        if (this.entries.putIfAbsent(holder, () -> func.apply(id)) != null) {
            throw new IllegalArgumentException("Duplicate registration " + name);
        }

        return holder;
    }

    @SuppressWarnings("unchecked")
    public void registerAll() {
        if (this.registered) {
            return;
        }

        this.registered = true;
        Registry<T> registry = (Registry<T>) BuiltInRegistries.REGISTRY.get(this.registryKey.location());

        if (registry == null) {
            throw new IllegalStateException("Registry " + this.registryKey.location() + " does not exist (yet). Create it before calling registerAll().");
        }

        this.entries.forEach((holder, sup) -> Registry.register(registry, holder.getKey(), sup.get()));
    }

    public Collection<DeferredHolder<T, ? extends T>> getEntries() {
        return this.entriesView;
    }

    public ResourceKey<? extends Registry<T>> getRegistryKey() {
        return this.registryKey;
    }

    public ResourceLocation getRegistryName() {
        return this.registryKey.location();
    }

    public String getNamespace() {
        return this.namespace;
    }
}
