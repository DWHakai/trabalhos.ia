package yesman.epicfight.fabric.mixin.client.event;

import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.platform.client.event.MovementInputUpdateEvent;

/// NeoForge: `MovementInputUpdateEvent` right after `Input#tick` in `LocalPlayer#aiStep`.
@Mixin(LocalPlayer.class)
public abstract class MixinLocalPlayerEvents {
    @Shadow public Input input;

    @Inject(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/Input;tick(ZF)V", shift = At.Shift.AFTER))
    private void epicfight$movementInputUpdate(CallbackInfo ci) {
        MovementInputUpdateEvent.BUS.post(new MovementInputUpdateEvent((LocalPlayer)(Object)this, this.input));
    }
}
