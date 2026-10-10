package yesman.epicfight.fabric.mixin.client.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.platform.client.event.InputEvent;

/// NeoForge: `InputEvent.MouseScrollingEvent` in `MouseHandler#onScroll`, in game, right before
/// the scroll is applied (hotbar slot / spectator menu / flying speed).
@Mixin(MouseHandler.class)
public abstract class MixinInputEvents {
    @Shadow @Final private Minecraft minecraft;
    @Shadow private boolean isLeftPressed;
    @Shadow private boolean isMiddlePressed;
    @Shadow private boolean isRightPressed;
    @Shadow private double xpos;
    @Shadow private double ypos;

    @Inject(method = "onScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isSpectator()Z"), cancellable = true)
    private void epicfight$mouseScroll(long windowPointer, double xOffset, double yOffset, CallbackInfo ci) {
        // Same deltas as the ones vanilla computed at the start of onScroll
        boolean discrete = this.minecraft.options.discreteMouseScroll().get();
        double sensitivity = this.minecraft.options.mouseWheelSensitivity().get();
        double deltaX = (discrete ? Math.signum(xOffset) : xOffset) * sensitivity;
        double deltaY = (discrete ? Math.signum(yOffset) : yOffset) * sensitivity;

        InputEvent.MouseScrollingEvent event = new InputEvent.MouseScrollingEvent(deltaX, deltaY, this.isLeftPressed, this.isMiddlePressed, this.isRightPressed, this.xpos, this.ypos);

        if (InputEvent.MouseScrollingEvent.BUS.post(event).isCanceled()) {
            ci.cancel();
        }
    }
}
