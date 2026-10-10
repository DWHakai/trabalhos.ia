package yesman.epicfight.fabric.mixin.client.event;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import yesman.epicfight.platform.client.event.RenderHandEvent;

/// NeoForge: `RenderHandEvent` before each first person hand (`ClientHooks#renderSpecificFirstPersonHand`).
@Mixin(ItemInHandRenderer.class)
public abstract class MixinItemInHandRendererEvents {
    @WrapWithCondition(
        method = "renderHandsWithItems",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;renderArmWithItem(Lnet/minecraft/client/player/AbstractClientPlayer;FFLnet/minecraft/world/InteractionHand;FLnet/minecraft/world/item/ItemStack;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V")
    )
    private boolean epicfight$renderHand(ItemInHandRenderer self, AbstractClientPlayer player, float partialTick, float pitch, InteractionHand hand, float swingProgress, ItemStack stack, float equipProgress, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        return !RenderHandEvent.BUS.post(new RenderHandEvent(hand, poseStack, buffer, packedLight, partialTick, pitch, swingProgress, equipProgress, stack)).isCanceled();
    }
}
