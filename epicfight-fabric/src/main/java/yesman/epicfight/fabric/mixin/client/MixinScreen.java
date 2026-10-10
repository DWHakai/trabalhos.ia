package yesman.epicfight.fabric.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import yesman.epicfight.fabric.ext.ScreenExtension;

@Mixin(Screen.class)
public abstract class MixinScreen implements ScreenExtension {
    @Shadow protected Minecraft minecraft;

    @Override
    public Minecraft getMinecraft() {
        return this.minecraft;
    }
}
