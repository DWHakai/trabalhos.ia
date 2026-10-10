package yesman.epicfight.fabric.mixin.client.event;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.platform.client.event.EntityRenderersEvent;

/// NeoForge: `EntityRenderersEvent.AddLayers` once the entity renderers were (re)created.
@Mixin(EntityRenderDispatcher.class)
public abstract class MixinEntityRenderDispatcherEvents {
    @Inject(method = "onResourceManagerReload", at = @At("TAIL"))
    private void epicfight$addLayers(ResourceManager resourceManager, CallbackInfo ci, @Local EntityRendererProvider.Context context) {
        EntityRenderDispatcher self = (EntityRenderDispatcher)(Object)this;
        EntityRenderersEvent.AddLayers.BUS.post(new EntityRenderersEvent.AddLayers(self.renderers, self.playerRenderers, context));
    }
}
