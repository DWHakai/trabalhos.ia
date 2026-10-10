package yesman.epicfight.fabric.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColors;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import yesman.epicfight.fabric.ext.MinecraftExtension;

@Mixin(Minecraft.class)
public abstract class MixinMinecraftExtension implements MinecraftExtension {
    @Shadow @Final private ItemColors itemColors;

    @Override
    public ItemColors getItemColors() {
        return this.itemColors;
    }
}
