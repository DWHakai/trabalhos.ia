package yesman.epicfight.fabric.mixin.event;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.platform.event.LivingJumpEvent;

/// NeoForge: `LivingJumpEvent` (`CommonHooks#onLivingJump`) once the jump impulse was applied.
@Mixin(LivingEntity.class)
public abstract class MixinLivingEntityJump {
    @Inject(method = "jumpFromGround", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/LivingEntity;hasImpulse:Z", opcode = org.objectweb.asm.Opcodes.PUTFIELD, shift = At.Shift.AFTER))
    private void epicfight$livingJump(CallbackInfo ci) {
        LivingJumpEvent.BUS.post(new LivingJumpEvent((LivingEntity)(Object)this));
    }
}
