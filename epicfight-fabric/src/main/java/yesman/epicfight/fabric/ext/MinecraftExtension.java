package yesman.epicfight.fabric.ext;

import net.minecraft.client.color.item.ItemColors;

/// NeoForge adds `Minecraft#getItemColors()`.
public interface MinecraftExtension {
    default ItemColors getItemColors() {
        throw new AssertionError("implemented by mixin");
    }
}
