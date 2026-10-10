package yesman.epicfight.registry.entries;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import yesman.epicfight.platform.registry.DeferredHolder;
import yesman.epicfight.platform.registry.DeferredRegister;
import yesman.epicfight.main.EpicFightMod;

public final class EpicFightCreativeTabs {
	private EpicFightCreativeTabs() {}
	
	public static final DeferredRegister<CreativeModeTab> REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, EpicFightMod.MODID);

	public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ITEMS = REGISTRY.register("items", () ->
		FabricItemGroup.builder()
			.title(Component.translatable("itemGroup.epicfight.items"))
			.icon(() -> new ItemStack(EpicFightItems.SKILLBOOK.get()))
			// Fabric port: Fabric pages modded tabs after the vanilla ones (NeoForge: withTabsBefore SPAWN_EGGS)
			.backgroundTexture(EpicFightMod.identifier("textures/gui/container/epicfight_creative_tab.png"))
			.hideTitle()
			.displayItems((params, output) -> {
				EpicFightItems.REGISTRY.getEntries().forEach(item -> {
					// FIXME: bad implement, maybe based protocol better yet.
					// ignore UCHIGATANA_SHEATH
					if (item == EpicFightItems.UCHIGATANA_SHEATH || item == EpicFightItems.SKILLBOOK) {
						return;
					}
					
					output.accept(item.get());
				});
			})
			.build()
		);
}