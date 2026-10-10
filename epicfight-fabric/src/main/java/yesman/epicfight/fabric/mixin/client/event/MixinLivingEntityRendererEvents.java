package yesman.epicfight.fabric.mixin.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.platform.client.event.RenderLivingEvent;

/// NeoForge: `RenderLivingEvent.Pre` at the start of `LivingEntityRenderer#render` (canceling
/// skips the rest) and `RenderLivingEvent.Post` at its end.
@Mixin(LivingEntityRenderer.class)
public abstract class MixinLivingEntityRendererEvents<T extends LivingEntity, M extends EntityModel<T>> {
    @Inject(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("HEAD"), cancellable = true)
    private void epicfight$renderPre(T entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
        @SuppressWarnings("unchecked")
        LivingEntityRenderer<T, M> self = (LivingEntityRenderer<T, M>)(Object)this;

        if (RenderLivingEvent.Pre.BUS.post(new RenderLivingEvent.Pre<>(entity, self, partialTick, poseStack, buffer, packedLight)).isCanceled()) {
            ci.cancel();
        }
    }

    @Inject(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("TAIL"))
    private void epicfight$renderPost(T entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
        @SuppressWarnings("unchecked")
        LivingEntityRenderer<T, M> self = (LivingEntityRenderer<T, M>)(Object)this;
        RenderLivingEvent.Post.BUS.post(new RenderLivingEvent.Post<>(entity, self, partialTick, poseStack, buffer, packedLight));
    }
}
