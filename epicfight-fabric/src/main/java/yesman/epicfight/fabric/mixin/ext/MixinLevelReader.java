package yesman.epicfight.fabric.mixin.ext;

import net.minecraft.world.level.LevelReader;
import org.spongepowered.asm.mixin.Mixin;
import yesman.epicfight.fabric.ext.LevelReaderExtension;

@Mixin(LevelReader.class)
public interface MixinLevelReader extends LevelReaderExtension {
}
