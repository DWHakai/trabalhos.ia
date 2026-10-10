package yesman.epicfight.fabric.mixin.client.event;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.fabric.EntityLevelHooks;

/// NeoForge: `Entity#onAddedToLevel` at the end of `ClientLevel#addEntity`.
@Mixin(ClientLevel.class)
public abstract class MixinClientLevelEvents {
    @Inject(method = "addEntity", at = @At("TAIL"))
    private void epicfight$onAddedToLevel(Entity entity, CallbackInfo ci) {
        EntityLevelHooks.onAddedToLevel(entity);
    }
}
