package yesman.epicfight.main;

import net.minecraft.world.item.CreativeModeTab;
import yesman.epicfight.platform.registry.DeferredHolder;
import yesman.epicfight.registry.entries.EpicFightCreativeTabs;

/**
 * @Param skillBookCreativeTab : decides which creative tab will display the skills that belong to the mod {@link EpicFightCreativeTabs}}
 */
public record EpicFightExtensions(DeferredHolder<CreativeModeTab, CreativeModeTab> skillBookCreativeTab) {
}