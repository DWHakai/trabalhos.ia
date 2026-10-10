package yesman.epicfight.fabric.mixin.event;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.fabric.EntityLevelHooks;

/// NeoForge calls `Entity#onAddedToLevel` on the server after an entity was added through
/// `ServerLevel#addFreshEntity`/`addPlayer` or loaded with its chunk. All of those end in this
/// manager's private `addEntity`.
@Mixin(PersistentEntitySectionManager.class)
public abstract class MixinPersistentEntitySectionManager<T extends EntityAccess> {
    @Inject(method = "addEntity", at = @At("RETURN"))
    private void epicfight$onAddedToLevel(T entity, boolean worldGenSpawned, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ() && entity instanceof Entity e) {
            EntityLevelHooks.onAddedToLevel(e);
        }
    }
}
