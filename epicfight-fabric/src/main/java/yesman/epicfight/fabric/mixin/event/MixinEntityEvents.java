package yesman.epicfight.fabric.mixin.event;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.api.event.impl.VanillaEntityEventHooks;
import yesman.epicfight.fabric.DropCapture;

import java.util.Collection;

/// NeoForge `Entity` patches:
///
/// - `EntityEvent.Size` and `EntityEvent.EntityConstructing` at the end of the constructor, and
///   `EntityEvent.Size` in `refreshDimensions`
/// - `EntityTickEvent.Pre/Post` around the tick of passengers (`rideTick`); the other entities
///   are ticked by `MixinServerLevelEvents` and `MixinClientLevelEvents`
/// - `EntityMountEvent` in `startRiding` and `removeVehicle`
/// - `captureDrops` in `spawnAtLocation`
@Mixin(Entity.class)
public abstract class MixinEntityEvents implements DropCapture {
    @Shadow
    private EntityDimensions dimensions;

    @Shadow
    private float eyeHeight;

    @Shadow
    private Entity vehicle;

    @Unique
    @Nullable
    private Collection<ItemEntity> epicfight$captureDrops;

    // NeoForge resizes before the eye height is computed; resizing here and computing it again
    // gives the same result since Mixin can only inject at the end of a constructor.
    @Inject(method = "<init>", at = @At("TAIL"))
    private void epicfight$onConstructed(CallbackInfo ci) {
        Entity self = (Entity)(Object)this;
        VanillaEntityEventHooks.onSizingEntity(self, newSize -> {
            this.dimensions = newSize;
            this.eyeHeight = newSize.eyeHeight();
        });
        VanillaEntityEventHooks.onConstruct(self);
    }

    @ModifyVariable(method = "refreshDimensions", at = @At("STORE"), ordinal = 1)
    private EntityDimensions epicfight$onSizing(EntityDimensions newDimensions) {
        EntityDimensions[] result = { newDimensions };
        VanillaEntityEventHooks.onSizingEntity((Entity)(Object)this, newSize -> result[0] = newSize);
        return result[0];
    }

    @WrapOperation(method = "rideTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;tick()V"))
    private void epicfight$tickPassenger(Entity entity, Operation<Void> original) {
        VanillaEntityEventHooks.preTick(entity);
        original.call(entity);
        VanillaEntityEventHooks.postTick(entity);
    }

    // NeoForge posts the event before checking whether the entity can ride; only successful
    // mounts are posted here, which is all Epic Fight's listener cares about.
    @Inject(method = "startRiding(Lnet/minecraft/world/entity/Entity;Z)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;isPassenger()Z", ordinal = 0))
    private void epicfight$onMount(Entity vehicle, boolean force, CallbackInfoReturnable<Boolean> cir) {
        VanillaEntityEventHooks.onEntityMount((Entity)(Object)this, vehicle, true);
    }

    @Inject(method = "removeVehicle", at = @At("HEAD"))
    private void epicfight$onDismount(CallbackInfo ci) {
        if (this.vehicle != null) {
            VanillaEntityEventHooks.onEntityMount((Entity)(Object)this, this.vehicle, false);
        }
    }

    @WrapOperation(method = "spawnAtLocation(Lnet/minecraft/world/item/ItemStack;F)Lnet/minecraft/world/entity/item/ItemEntity;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean epicfight$captureDrop(Level level, Entity itemEntity, Operation<Boolean> original) {
        if (this.epicfight$captureDrops != null) {
            this.epicfight$captureDrops.add((ItemEntity)itemEntity);
            return true;
        }

        return original.call(level, itemEntity);
    }

    @Override
    @Nullable
    public Collection<ItemEntity> epicfight$captureDrops() {
        return this.epicfight$captureDrops;
    }

    @Override
    @Nullable
    public Collection<ItemEntity> epicfight$captureDrops(@Nullable Collection<ItemEntity> value) {
        Collection<ItemEntity> previous = this.epicfight$captureDrops;
        this.epicfight$captureDrops = value;
        return previous;
    }
}
