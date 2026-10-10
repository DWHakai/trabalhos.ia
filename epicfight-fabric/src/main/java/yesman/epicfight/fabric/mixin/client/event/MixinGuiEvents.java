package yesman.epicfight.fabric.mixin.client.event;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.platform.client.event.GuiLayers;
import yesman.epicfight.platform.client.event.RenderGuiEvent;

/// NeoForge: `RenderGuiEvent.Pre`/`Post` around the HUD layers (canceling `Pre` hides the HUD), and
/// the layers registered above all the vanilla ones (`RegisterGuiLayersEvent#registerAboveAll`).
@Mixin(Gui.class)
public abstract class MixinGuiEvents {
    @Shadow @Final private Minecraft minecraft;
    @Shadow @Final private LayeredDraw layers;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void epicfight$addModLayers(Minecraft minecraft, CallbackInfo ci) {
        this.layers.add(new LayeredDraw().add(GuiLayers::render), () -> !this.minecraft.options.hideGui);
    }

    @WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/LayeredDraw;render(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V"))
    private void epicfight$renderGui(LayeredDraw layers, GuiGraphics guiGraphics, DeltaTracker deltaTracker, Operation<Void> original) {
        if (RenderGuiEvent.Pre.BUS.post(new RenderGuiEvent.Pre(guiGraphics, deltaTracker)).isCanceled()) {
            return;
        }

        original.call(layers, guiGraphics, deltaTracker);
        RenderGuiEvent.Post.BUS.post(new RenderGuiEvent.Post(guiGraphics, deltaTracker));
    }
}
