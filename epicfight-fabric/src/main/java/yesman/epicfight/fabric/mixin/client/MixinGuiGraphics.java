package yesman.epicfight.fabric.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import yesman.epicfight.fabric.ext.GuiGraphicsExtension;

/// The float `drawString` overloads NeoForge adds, same bodies as the vanilla int ones.
@Mixin(GuiGraphics.class)
public abstract class MixinGuiGraphics implements GuiGraphicsExtension {
    @Shadow @Final private PoseStack pose;
    @Shadow @Final private MultiBufferSource.BufferSource bufferSource;

    @Shadow protected abstract void flushIfUnmanaged();

    @Override
    public int drawString(Font font, String text, float x, float y, int color, boolean dropShadow) {
        if (text == null) {
            return 0;
        }

        int i = font.drawInBatch(text, x, y, color, dropShadow, this.pose.last().pose(), this.bufferSource, Font.DisplayMode.NORMAL, 0, 15728880, font.isBidirectional());
        this.flushIfUnmanaged();
        return i;
    }

    @Override
    public int drawString(Font font, FormattedCharSequence text, float x, float y, int color, boolean dropShadow) {
        int i = font.drawInBatch(text, x, y, color, dropShadow, this.pose.last().pose(), this.bufferSource, Font.DisplayMode.NORMAL, 0, 15728880);
        this.flushIfUnmanaged();
        return i;
    }
}
