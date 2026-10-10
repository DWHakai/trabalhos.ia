package yesman.epicfight.fabric.mixin.event;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import yesman.epicfight.api.event.impl.VanillaEntityEventHooks;

/// NeoForge: `EntityTickEvent.Pre/Post` around `Entity#tick` in `ServerLevel#tickNonPassenger`.
/// Passengers are ticked through `Entity#rideTick` (`MixinEntityEvents`).
@Mixin(ServerLevel.class)
public abstract class MixinServerLevelEvents {
    @WrapOperation(method = "tickNonPassenger", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;tick()V"))
    private void epicfight$tickEntity(Entity entity, Operation<Void> original) {
        VanillaEntityEventHooks.preTick(entity);
        original.call(entity);
        VanillaEntityEventHooks.postTick(entity);
    }
}
