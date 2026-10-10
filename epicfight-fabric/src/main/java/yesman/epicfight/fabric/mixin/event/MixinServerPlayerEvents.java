package yesman.epicfight.fabric.mixin.event;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.DimensionTransition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.api.event.impl.VanillaEntityEventHooks;
import yesman.epicfight.api.event.impl.VanillaPlayerEventHooks;
import yesman.epicfight.fabric.DropCapture;

import java.util.Collection;

/// NeoForge `ServerPlayer` patches:
///
/// - `LivingDeathEvent`: `ServerPlayer#die` doesn't call super
/// - `captureDrops` in `drop(ItemStack, boolean, boolean)`, used for the inventory dropped on death
/// - `PlayerEvent.PlayerChangedDimensionEvent` at the end of a dimension change. Fabric's
///   `AFTER_PLAYER_CHANGE_WORLD` is also posted when respawning in another dimension, which
///   NeoForge reports as a respawn instead.
@Mixin(ServerPlayer.class)
public abstract class MixinServerPlayerEvents {
    @Inject(method = "die", at = @At("HEAD"))
    private void epicfight$onDeath(DamageSource damageSource, CallbackInfo ci) {
        VanillaEntityEventHooks.onLivingDeath((ServerPlayer)(Object)this, damageSource);
    }

    @WrapOperation(method = "drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean epicfight$captureDrop(Level level, Entity itemEntity, Operation<Boolean> original) {
        Collection<ItemEntity> captureDrops = ((DropCapture)this).epicfight$captureDrops();

        if (captureDrops != null) {
            captureDrops.add((ItemEntity)itemEntity);
            return true;
        }

        return original.call(level, itemEntity);
    }

    @Inject(method = "changeDimension", at = @At(value = "FIELD", target = "Lnet/minecraft/server/level/ServerPlayer;lastSentFood:I", opcode = org.objectweb.asm.Opcodes.PUTFIELD, shift = At.Shift.AFTER))
    private void epicfight$onChangedDimension(DimensionTransition transition, CallbackInfoReturnable<Entity> cir) {
        VanillaPlayerEventHooks.onChagneDimension((ServerPlayer)(Object)this);
    }
}
