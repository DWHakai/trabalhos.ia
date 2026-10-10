package yesman.epicfight.fabric.ext;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import org.jetbrains.annotations.Nullable;

/// NeoForge lets a `BlockParticleOption` carry the position of its block (client side only, not
/// serialized).
public interface BlockParticleOptionExtension {
    default BlockParticleOption setPos(BlockPos pos) {
        throw new AssertionError("implemented by mixin");
    }

    @Nullable
    default BlockPos getPos() {
        throw new AssertionError("implemented by mixin");
    }
}
