package yesman.epicfight.fabric.mixin.event;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.api.event.impl.VanillaEntityEventHooks;
import yesman.epicfight.fabric.EntityLevelHooks;

/// Server side entities enter the level through this manager's private `addEntity`: entities
/// added with `ServerLevel#addFreshEntity`/`addPlayer` and the ones loaded with their chunk.
///
/// - NeoForge posts `EntityJoinLevelEvent` at its start, and cancelling it stops the entity from
///   being added (Epic Fight cancels endermen on the main End island during the boss fight)
/// - NeoForge calls `Entity#onAddedToLevel` once the entity was added
@Mixin(PersistentEntitySectionManager.class)
public abstract class MixinPersistentEntitySectionManager<T extends EntityAccess> {
    @Inject(method = "addEntity", at = @At("HEAD"), cancellable = true)
    private void epicfight$onJoinLevel(T entityAccess, boolean worldGenSpawned, CallbackInfoReturnable<Boolean> cir) {
        if (entityAccess instanceof Entity entity) {
            VanillaEntityEventHooks.onJoinLevel(entity, entity.level(), worldGenSpawned);

            if (entity.getType() == EntityType.ENDERMAN && VanillaEntityEventHooks.onEnderManSapwns((EnderMan)entity)) {
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(method = "addEntity", at = @At("RETURN"))
    private void epicfight$onAddedToLevel(T entity, boolean worldGenSpawned, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ() && entity instanceof Entity e) {
            EntityLevelHooks.onAddedToLevel(e);
        }
    }
}
