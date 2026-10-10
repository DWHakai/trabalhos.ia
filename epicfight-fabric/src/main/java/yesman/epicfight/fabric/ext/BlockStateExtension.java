package yesman.epicfight.fabric.ext;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.jetbrains.annotations.Nullable;

/// NeoForge's position-aware `IBlockStateExtension` overloads, with NeoForge's default behaviour
/// (vanilla blocks never override them). Injected into `BlockBehaviour.BlockStateBase`.
public interface BlockStateExtension {
    default boolean isBed(BlockGetter level, BlockPos pos, @Nullable LivingEntity sleeper) {
        return ((BlockBehaviour.BlockStateBase)this).getBlock() instanceof BedBlock;
    }

    default int getLightEmission(BlockGetter level, BlockPos pos) {
        return ((BlockBehaviour.BlockStateBase)this).getLightEmission();
    }

    default SoundType getSoundType(LevelReader level, BlockPos pos, @Nullable Entity entity) {
        return ((BlockBehaviour.BlockStateBase)this).getSoundType();
    }
}
