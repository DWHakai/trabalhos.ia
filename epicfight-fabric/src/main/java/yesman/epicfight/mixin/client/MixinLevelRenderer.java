package yesman.epicfight.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.world.entity.Entity;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.api.client.camera.EpicFightCameraAPI;
import yesman.epicfight.config.ClientConfig;

@Mixin(value = LevelRenderer.class)
public class MixinLevelRenderer {
	@Shadow @Final
	private RenderBuffers renderBuffers;
	
	@Shadow @Final
	private Minecraft minecraft;
	
	// Fabric port: the entity is captured with MixinExtras instead of the full local variable
	// table (LocalCapture.CAPTURE_FAILHARD), which other mods injecting here can shift
	@Inject(
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/OutlineBufferSource;setColor(IIII)V",
			shift = Shift.AFTER
		),
		method = "renderLevel(Lnet/minecraft/client/DeltaTracker;ZLnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/GameRenderer;Lnet/minecraft/client/renderer/LightTexture;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;)V"
	)
	private void epicfight$renderLevel(DeltaTracker deltaTracker, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f frustumMatrix, Matrix4f projectionMatrix, CallbackInfo callbackInfo, @Local Entity entity) {
        int color = ClientConfig.packedTargetOutlineColor;
        int r = color >> 16 & 255;
        int g = color >> 8 & 255;
        int b = color & 255;
        if (EpicFightCameraAPI.getInstance().shouldHighlightTarget(entity)) this.renderBuffers.outlineBufferSource().setColor(r, g, b, 255);
	}
}
