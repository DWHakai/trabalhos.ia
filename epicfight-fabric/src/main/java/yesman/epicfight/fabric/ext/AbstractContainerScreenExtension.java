package yesman.epicfight.fabric.ext;

import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;

/// NeoForge adds `AbstractContainerScreen#getSlotUnderMouse()`.
public interface AbstractContainerScreenExtension {
    @Nullable
    default Slot getSlotUnderMouse() {
        throw new AssertionError("implemented by mixin");
    }
}
