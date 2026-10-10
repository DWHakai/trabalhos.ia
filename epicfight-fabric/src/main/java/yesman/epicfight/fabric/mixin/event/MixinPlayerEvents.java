package yesman.epicfight.fabric.mixin.event;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.api.event.impl.VanillaEntityEventHooks;

/// NeoForge `LivingDamageEvent.Pre/Post` in `Player#actuallyHurt`, which replaces the method of
/// `LivingEntity` (see `MixinLivingEntityEvents`). `Player#die` calls super, so its death event
/// comes from `LivingEntity#die`.
@Mixin(Player.class)
public abstract class MixinPlayerEvents {
    @Unique
    private float epicfight$healthDamage = Float.NaN;

    @WrapOperation(method = "actuallyHurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getDamageAfterMagicAbsorb(Lnet/minecraft/world/damagesource/DamageSource;F)F"))
    private float epicfight$onDamagePre(Player self, DamageSource damageSource, float amount, Operation<Float> original) {
        float[] damage = { original.call(self, damageSource, amount) };
        VanillaEntityEventHooks.onCalculateDamagePre(self, damageSource, damage[0], newDamage -> damage[0] = newDamage);
        return damage[0];
    }

    @ModifyExpressionValue(method = "actuallyHurt", at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(FF)F"))
    private float epicfight$captureHealthDamage(float healthDamage) {
        this.epicfight$healthDamage = healthDamage;
        return healthDamage;
    }

    @Inject(method = "actuallyHurt", at = @At("TAIL"))
    private void epicfight$onDamagePost(DamageSource damageSource, float amount, CallbackInfo ci) {
        float healthDamage = this.epicfight$healthDamage;
        this.epicfight$healthDamage = Float.NaN;

        if (!Float.isNaN(healthDamage)) {
            VanillaEntityEventHooks.onCalculateDamagePost((Player)(Object)this, damageSource, healthDamage);
        }
    }
}
