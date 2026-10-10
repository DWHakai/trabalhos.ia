package yesman.epicfight.fabric.ext;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

/// NeoForge adds float-position overloads of `GuiGraphics#drawString`; injected into `GuiGraphics`
/// and implemented by `MixinGuiGraphics`.
public interface GuiGraphicsExtension {
    default int drawString(Font font, @Nullable String text, float x, float y, int color, boolean dropShadow) {
        throw new AssertionError("implemented by mixin");
    }

    default int drawString(Font font, FormattedCharSequence text, float x, float y, int color, boolean dropShadow) {
        throw new AssertionError("implemented by mixin");
    }

    default int drawString(Font font, Component text, float x, float y, int color, boolean dropShadow) {
        return this.drawString(font, text.getVisualOrderText(), x, y, color, dropShadow);
    }
}
