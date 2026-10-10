package yesman.epicfight.fabric.mixin.event;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.api.event.impl.VanillaEntityEventHooks;
import yesman.epicfight.fabric.DropCapture;
import yesman.epicfight.fabric.FabricCommonEvents;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/// NeoForge `LivingEntity` patches: `LivingDeathEvent`, `LivingKnockBackEvent`,
/// `LivingShieldBlockEvent`, `LivingDamageEvent.Pre/Post`, `LivingDropsEvent`,
/// `MobEffectEvent.Added/Remove/Expired` and `LivingEntityUseItemEvent.Start`.
///
/// `LivingIncomingDamageEvent` and `LivingEquipmentChangeEvent` come from Fabric API callbacks
/// (see `FabricCommonEvents`). `Player#actuallyHurt` doesn't call super and has its own copy of
/// the damage hooks in `MixinPlayerEvents`.
@Mixin(LivingEntity.class)
public abstract class MixinLivingEntityEvents {
    @Shadow
    @Final
    private Map<Holder<MobEffect>, MobEffectInstance> activeEffects;

    /// Health damage of the running `actuallyHurt`, NaN outside of it or when the entity is
    /// invulnerable to the damage
    @Unique
    private float epicfight$healthDamage = Float.NaN;

    @Inject(method = "die", at = @At("HEAD"))
    private void epicfight$onDeath(DamageSource damageSource, CallbackInfo ci) {
        VanillaEntityEventHooks.onLivingDeath((LivingEntity)(Object)this, damageSource);
    }

    @Inject(method = "knockback(DDD)V", at = @At("HEAD"), cancellable = true)
    private void epicfight$onKnockBack(double strength, double x, double z, CallbackInfo ci) {
        if (VanillaEntityEventHooks.onKnockedBack((LivingEntity)(Object)this)) {
            ci.cancel();
        }
    }

    // NeoForge 1.21.1 posts LivingShieldBlockEvent for every damage above 0, telling the
    // listeners whether the shield blocked it, and the upstream listener plays the shield hit
    // animation without checking that. It is posted only for blocked damage here, which is what
    // the listener means to react to.
    @WrapOperation(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isDamageSourceBlocked(Lnet/minecraft/world/damagesource/DamageSource;)Z"))
    private boolean epicfight$onShieldBlock(LivingEntity self, DamageSource damageSource, Operation<Boolean> original) {
        boolean blocked = original.call(self, damageSource);

        if (blocked) {
            VanillaEntityEventHooks.onBlockAttacksWithShield(self);
        }

        return blocked;
    }

    // LivingDamageEvent.Pre: after armor and magic reductions, before absorption
    @WrapOperation(method = "actuallyHurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getDamageAfterMagicAbsorb(Lnet/minecraft/world/damagesource/DamageSource;F)F"))
    private float epicfight$onDamagePre(LivingEntity self, DamageSource damageSource, float amount, Operation<Float> original) {
        float[] damage = { original.call(self, damageSource, amount) };
        VanillaEntityEventHooks.onCalculateDamagePre(self, damageSource, damage[0], newDamage -> damage[0] = newDamage);
        return damage[0];
    }

    @ModifyExpressionValue(method = "actuallyHurt", at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(FF)F"))
    private float epicfight$captureHealthDamage(float healthDamage) {
        this.epicfight$healthDamage = healthDamage;
        return healthDamage;
    }

    // LivingDamageEvent.Post: once the health was reduced, with the damage that went past absorption
    @Inject(method = "actuallyHurt", at = @At("TAIL"))
    private void epicfight$onDamagePost(DamageSource damageSource, float amount, CallbackInfo ci) {
        float healthDamage = this.epicfight$healthDamage;
        this.epicfight$healthDamage = Float.NaN;

        if (!Float.isNaN(healthDamage)) {
            VanillaEntityEventHooks.onCalculateDamagePost((LivingEntity)(Object)this, damageSource, healthDamage);
        }
    }

    @Inject(method = "dropAllDeathLoot", at = @At("HEAD"))
    private void epicfight$startCapturingDrops(ServerLevel level, DamageSource damageSource, CallbackInfo ci) {
        ((DropCapture)this).epicfight$captureDrops(new ArrayList<>());
    }

    @Inject(method = "dropAllDeathLoot", at = @At("TAIL"))
    private void epicfight$onDrops(ServerLevel level, DamageSource damageSource, CallbackInfo ci) {
        Collection<ItemEntity> drops = ((DropCapture)this).epicfight$captureDrops(null);

        if (drops != null && !VanillaEntityEventHooks.onDropItems((LivingEntity)(Object)this, damageSource, drops)) {
            drops.forEach(level::addFreshEntity);
        }
    }

    @Inject(method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z", at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;", ordinal = 0))
    private void epicfight$onEffectAdded(MobEffectInstance effectInstance, Entity source, CallbackInfoReturnable<Boolean> cir) {
        VanillaEntityEventHooks.onMobEffectAdded(effectInstance, (LivingEntity)(Object)this);
    }

    @Inject(method = "removeEffect", at = @At("HEAD"))
    private void epicfight$onEffectRemoved(Holder<MobEffect> effect, CallbackInfoReturnable<Boolean> cir) {
        VanillaEntityEventHooks.onMobEffectRemoved(this.activeEffects.get(effect), (LivingEntity)(Object)this);
    }

    @WrapOperation(method = "removeAllEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;onEffectRemoved(Lnet/minecraft/world/effect/MobEffectInstance;)V"))
    private void epicfight$onAllEffectsRemoved(LivingEntity self, MobEffectInstance effectInstance, Operation<Void> original) {
        VanillaEntityEventHooks.onMobEffectRemoved(effectInstance, self);
        original.call(self, effectInstance);
    }

    // Only reached on the server, like the NeoForge event
    @WrapOperation(method = "tickEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;onEffectRemoved(Lnet/minecraft/world/effect/MobEffectInstance;)V"))
    private void epicfight$onEffectExpired(LivingEntity self, MobEffectInstance effectInstance, Operation<Void> original) {
        VanillaEntityEventHooks.onMobEffectExpired(effectInstance, self);
        original.call(self, effectInstance);
    }

    @Inject(method = "startUsingItem", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/LivingEntity;useItem:Lnet/minecraft/world/item/ItemStack;", opcode = org.objectweb.asm.Opcodes.PUTFIELD), cancellable = true)
    private void epicfight$onItemUseStart(InteractionHand hand, CallbackInfo ci) {
        LivingEntity self = (LivingEntity)(Object)this;

        if (FabricCommonEvents.onItemUseStart(self, self.getItemInHand(hand))) {
            ci.cancel();
        }
    }
}
