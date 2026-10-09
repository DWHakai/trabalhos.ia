package yesman.epicfight.platform.registry;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

/// Fabric replacement for NeoForge's `DeferredHolder`: a [Holder] that resolves lazily to the
/// registry entry with the given key, so it can be created before the entry is registered.
public class DeferredHolder<R, T extends R> implements Holder<R>, Supplier<T> {
    protected final ResourceKey<R> key;
    @Nullable
    private Holder<R> holder;

    public static <R, T extends R> DeferredHolder<R, T> create(ResourceKey<R> key) {
        return new DeferredHolder<>(key);
    }

    public static <R, T extends R> DeferredHolder<R, T> create(ResourceKey<? extends Registry<R>> registryKey, ResourceLocation valueName) {
        return create(ResourceKey.create(registryKey, valueName));
    }

    protected DeferredHolder(ResourceKey<R> key) {
        this.key = Objects.requireNonNull(key);
    }

    @SuppressWarnings("unchecked")
    @Override
    public R value() {
        this.bind(true);
        return Objects.requireNonNull(this.holder, () -> "Trying to access unbound value: " + this.key).value();
    }

    @SuppressWarnings("unchecked")
    @Override
    public T get() {
        return (T) this.value();
    }

    public Optional<T> asOptional() {
        return this.isBound() ? Optional.of(this.get()) : Optional.empty();
    }

    @SuppressWarnings("unchecked")
    @Nullable
    protected Registry<R> getRegistry() {
        return (Registry<R>) BuiltInRegistries.REGISTRY.get(this.key.registry());
    }

    protected final void bind(boolean throwOnMissingRegistry) {
        if (this.holder != null) {
            return;
        }

        Registry<R> registry = this.getRegistry();

        if (registry != null) {
            this.holder = registry.getHolder(this.key).orElse(null);
        } else if (throwOnMissingRegistry) {
            throw new IllegalStateException("Registry not present for " + this + ": " + this.key.registry());
        }
    }

    public ResourceLocation getId() {
        return this.key.location();
    }

    public ResourceKey<R> getKey() {
        return this.key;
    }

    public Holder<R> getDelegate() {
        this.bind(true);
        return Objects.requireNonNull(this.holder, () -> "Trying to access unbound holder: " + this.key);
    }

    @Override
    public boolean isBound() {
        this.bind(false);
        return this.holder != null && this.holder.isBound();
    }

    @Override
    public boolean is(ResourceLocation id) {
        return id.equals(this.key.location());
    }

    @Override
    public boolean is(ResourceKey<R> key) {
        return key == this.key;
    }

    @Override
    public boolean is(Predicate<ResourceKey<R>> filter) {
        return filter.test(this.key);
    }

    @Override
    public boolean is(TagKey<R> tag) {
        this.bind(false);
        return this.holder != null && this.holder.is(tag);
    }

    // Not annotated with @Override: present on Holder in some Minecraft versions only.
    public boolean is(Holder<R> holder) {
        return holder.unwrapKey().filter(this.key::equals).isPresent();
    }

    @Override
    public Stream<TagKey<R>> tags() {
        this.bind(false);
        return this.holder != null ? this.holder.tags() : Stream.empty();
    }

    @Override
    public Either<ResourceKey<R>, R> unwrap() {
        return Either.left(this.key);
    }

    @Override
    public Optional<ResourceKey<R>> unwrapKey() {
        return Optional.of(this.key);
    }

    @Override
    public Kind kind() {
        return Kind.REFERENCE;
    }

    @Override
    public boolean canSerializeIn(HolderOwner<R> owner) {
        this.bind(false);
        return this.holder != null && this.holder.canSerializeIn(owner);
    }

    // Not annotated with @Override: a default method on Holder.
    public String getRegisteredName() {
        return this.key.location().toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        return obj instanceof Holder<?> h && h.kind() == Kind.REFERENCE && h.unwrapKey().filter(this.key::equals).isPresent();
    }

    @Override
    public int hashCode() {
        return this.key.hashCode();
    }

    @Override
    public String toString() {
        return String.format(java.util.Locale.ENGLISH, "DeferredHolder{%s}", this.key);
    }
}
