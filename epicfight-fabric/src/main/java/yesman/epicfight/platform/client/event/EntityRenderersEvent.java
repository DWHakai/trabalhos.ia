package yesman.epicfight.platform.client.event;

import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import yesman.epicfight.platform.event.ListenerList;

import java.util.Map;
import java.util.Set;

/// NeoForge `EntityRenderersEvent` (only `AddLayers` is used by Epic Fight).
public abstract class EntityRenderersEvent {
    /// Fired at the end of `EntityRenderDispatcher#onResourceManagerReload`, after the entity
    /// renderers were (re)created.
    public static class AddLayers extends EntityRenderersEvent {
        public static final ListenerList<AddLayers> BUS = new ListenerList<>();

        private final Map<EntityType<?>, EntityRenderer<?>> renderers;
        private final Map<PlayerSkin.Model, EntityRenderer<? extends Player>> skinMap;
        private final EntityRendererProvider.Context context;

        public AddLayers(Map<EntityType<?>, EntityRenderer<?>> renderers, Map<PlayerSkin.Model, EntityRenderer<? extends Player>> playerRenderers, EntityRendererProvider.Context context) {
            this.renderers = renderers;
            this.skinMap = playerRenderers;
            this.context = context;
        }

        public Set<PlayerSkin.Model> getSkins() {
            return this.skinMap.keySet();
        }

        @Nullable
        @SuppressWarnings("unchecked")
        public <R extends EntityRenderer<? extends Player>> R getSkin(PlayerSkin.Model skinModel) {
            return (R)this.skinMap.get(skinModel);
        }

        public Set<EntityType<?>> getEntityTypes() {
            return this.renderers.keySet();
        }

        @Nullable
        @SuppressWarnings("unchecked")
        public <T extends Entity, R extends EntityRenderer<T>> R getRenderer(EntityType<? extends T> entityType) {
            return (R)this.renderers.get(entityType);
        }

        public EntityModelSet getEntityModels() {
            return this.context.getModelSet();
        }

        public EntityRendererProvider.Context getContext() {
            return this.context;
        }
    }
}
