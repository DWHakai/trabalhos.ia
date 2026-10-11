package yesman.epicfight.fabric.mixin.client.event;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.BossEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import yesman.epicfight.platform.client.event.CustomizeGuiOverlayEvent;

/// NeoForge: `CustomizeGuiOverlayEvent.BossEventProgress` before each boss bar. Canceling skips
/// the bar and its name; the increment moves the next bar.
@Mixin(BossHealthOverlay.class)
public abstract class MixinBossHealthOverlayEvents {
    @Shadow @Final private Minecraft minecraft;

    @Unique
    private CustomizeGuiOverlayEvent.BossEventProgress epicfight$currentEvent;

    @WrapWithCondition(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/BossHealthOverlay;drawBar(Lnet/minecraft/client/gui/GuiGraphics;IILnet/minecraft/world/BossEvent;)V"))
    private boolean epicfight$bossEventProgress(BossHealthOverlay self, GuiGraphics guiGraphics, int x, int y, BossEvent bossEvent) {
        this.epicfight$currentEvent = CustomizeGuiOverlayEvent.BossEventProgress.BUS.post(
            new CustomizeGuiOverlayEvent.BossEventProgress(this.minecraft.getWindow(), guiGraphics, (LerpingBossEvent)bossEvent, x, y, 10 + this.minecraft.font.lineHeight)
        );

        return !this.epicfight$currentEvent.isCanceled();
    }

    @WrapWithCondition(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)I"))
    private boolean epicfight$bossName(GuiGraphics guiGraphics, Font font, Component text, int x, int y, int color) {
        return this.epicfight$currentEvent == null || !this.epicfight$currentEvent.isCanceled();
    }

    // Vanilla: `j += 10 + this.minecraft.font.lineHeight` (the 10 is the only one in `render`);
    // NeoForge adds the event's increment instead.
    @ModifyConstant(method = "render", constant = @Constant(intValue = 10))
    private int epicfight$increment(int increment) {
        return this.epicfight$currentEvent == null ? increment : this.epicfight$currentEvent.getIncrement() - this.minecraft.font.lineHeight;
    }
}
