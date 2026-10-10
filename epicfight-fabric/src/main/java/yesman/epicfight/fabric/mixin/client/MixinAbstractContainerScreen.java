package yesman.epicfight.fabric.mixin.client;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import yesman.epicfight.fabric.ext.AbstractContainerScreenExtension;

@Mixin(AbstractContainerScreen.class)
public abstract class MixinAbstractContainerScreen implements AbstractContainerScreenExtension {
    @Shadow protected Slot hoveredSlot;

    @Override
    public Slot getSlotUnderMouse() {
        return this.hoveredSlot;
    }
}
