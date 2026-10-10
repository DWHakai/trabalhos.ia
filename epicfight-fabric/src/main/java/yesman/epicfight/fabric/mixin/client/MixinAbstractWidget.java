package yesman.epicfight.fabric.mixin.client;

import net.minecraft.client.gui.components.AbstractWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import yesman.epicfight.fabric.ext.AbstractWidgetExtension;

@Mixin(AbstractWidget.class)
public abstract class MixinAbstractWidget implements AbstractWidgetExtension {
    @Shadow public boolean active;

    @Unique
    protected int epicfight$packedFGColor = UNSET_FG_COLOR;

    @Override
    public int getFGColor() {
        if (this.epicfight$packedFGColor != UNSET_FG_COLOR) {
            return this.epicfight$packedFGColor;
        }

        return this.active ? 16777215 : 10526880; // White : Light Grey
    }

    @Override
    public void setFGColor(int color) {
        this.epicfight$packedFGColor = color;
    }

    @Override
    public int getPackedFGColor() {
        return this.epicfight$packedFGColor;
    }
}
