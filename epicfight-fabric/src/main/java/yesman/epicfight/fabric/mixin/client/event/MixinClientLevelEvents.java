package yesman.epicfight.fabric.mixin.client.event;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.EnderMan;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.api.event.impl.VanillaEntityEventHooks;
import yesman.epicfight.client.world.util.FakeLevel;
import yesman.epicfight.fabric.EntityLevelHooks;

/// NeoForge `ClientLevel` patches:
///
/// - `LevelEvent.Load` at the end of the constructor (upstream
///   `NeoForgeWorldEvent.WorldEventsClient#loadLevel` creates the [FakeLevel] of the new level)
/// - `EntityTickEvent.Pre/Post` around `Entity#tick` in `tickNonPassenger`
/// - `EntityJoinLevelEvent` at the start of `addEntity`, and `Entity#onAddedToLevel` at its end
@Mixin(ClientLevel.class)
public abstract class MixinClientLevelEvents {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void epicfight$loadLevel(CallbackInfo ci) {
        // Prevent infinite loop
        if (!((Object)this instanceof FakeLevel)) {
            FakeLevel.getFakeLevel((ClientLevel)(Object)this);
        }
    }

    @WrapOperation(method = "tickNonPassenger", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;tick()V"))
    private void epicfight$tickEntity(Entity entity, Operation<Void> original) {
        VanillaEntityEventHooks.preTick(entity);
        original.call(entity);
        VanillaEntityEventHooks.postTick(entity);
    }

    @Inject(method = "addEntity", at = @At("HEAD"), cancellable = true)
    private void epicfight$onJoinLevel(Entity entity, CallbackInfo ci) {
        VanillaEntityEventHooks.onJoinLevel(entity, (ClientLevel)(Object)this, false);

        if (entity.getType() == EntityType.ENDERMAN && VanillaEntityEventHooks.onEnderManSapwns((EnderMan)entity)) {
            ci.cancel();
        }
    }

    @Inject(method = "addEntity", at = @At("TAIL"))
    private void epicfight$onAddedToLevel(Entity entity, CallbackInfo ci) {
        EntityLevelHooks.onAddedToLevel(entity);
    }
}
