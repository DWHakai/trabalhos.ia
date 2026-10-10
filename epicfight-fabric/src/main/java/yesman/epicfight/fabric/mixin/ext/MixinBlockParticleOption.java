package yesman.epicfight.fabric.mixin.ext;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import yesman.epicfight.fabric.ext.BlockParticleOptionExtension;

@Mixin(BlockParticleOption.class)
public abstract class MixinBlockParticleOption implements BlockParticleOptionExtension {
    @Unique
    private BlockPos epicfight$pos;

    @Override
    public BlockParticleOption setPos(BlockPos pos) {
        this.epicfight$pos = pos;
        return (BlockParticleOption)(Object)this;
    }

    @Override
    public BlockPos getPos() {
        return this.epicfight$pos;
    }
}
