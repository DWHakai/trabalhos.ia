package yesman.epicfight.fabric.ext;

import net.minecraft.client.Minecraft;

/// NeoForge adds `Screen#getMinecraft()`; injected by Loom, implemented by `MixinScreen`.
public interface ScreenExtension {
    default Minecraft getMinecraft() {
        throw new AssertionError("implemented by mixin");
    }
}
