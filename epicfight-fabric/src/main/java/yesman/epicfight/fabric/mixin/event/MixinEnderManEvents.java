package yesman.epicfight.fabric.mixin.event;

import net.minecraft.world.entity.monster.EnderMan;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.api.event.impl.VanillaEntityEventHooks;

/// NeoForge: `EntityTeleportEvent.EnderEntity` in `EnderMan#teleport(double, double, double)`;
/// a canceled teleport fails.
@Mixin(EnderMan.class)
public abstract class MixinEnderManEvents {
    @Inject(method = "teleport(DDD)Z", at = @At("HEAD"), cancellable = true)
    private void epicfight$onTeleport(double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
        if (VanillaEntityEventHooks.onEndermanTeleports((EnderMan)(Object)this)) {
            cir.setReturnValue(false);
        }
    }
}
