package yesman.epicfight.fabric.ext;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.LevelReader;

import java.util.Optional;

/// NeoForge's `ILevelReaderExtension` registry shortcuts, injected into `LevelReader`.
public interface LevelReaderExtension {
    default <T> Holder<T> holderOrThrow(ResourceKey<T> key) {
        return ((LevelReader)this).registryAccess().holderOrThrow(key);
    }

    default <T> Optional<Holder.Reference<T>> holder(ResourceKey<T> key) {
        return ((LevelReader)this).registryAccess().holder(key);
    }
}
