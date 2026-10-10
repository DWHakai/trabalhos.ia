package yesman.epicfight.fabric;

import net.minecraft.world.entity.Entity;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.EntityPatch;

/// NeoForge's `Entity#onAddedToLevel`, which upstream `MixinEntity` hooks to notify the entity
/// patch. Called by `MixinPersistentEntitySectionManager` (server) and `MixinClientLevelEvents`.
public final class EntityLevelHooks {
    public static void onAddedToLevel(Entity entity) {
        EntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(entity, EntityPatch.class);

        if (entitypatch != null) {
            entitypatch.onAddedToLevel();
        }
    }

    private EntityLevelHooks() {}
}
