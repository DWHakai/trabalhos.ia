package yesman.epicfight.fabric.mixin.ext;

import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import yesman.epicfight.fabric.ext.BlockStateExtension;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class MixinBlockStateBase implements BlockStateExtension {
}
