package yesman.epicfight.fabric.mixin.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.platform.client.event.RenderLevelStageEvent;

/// NeoForge: `RenderLevelStageEvent` for the tripwire layer, at the end of `renderSectionLayer`
/// (render type stages have an empty pose stack there too).
@Mixin(LevelRenderer.class)
public abstract class MixinLevelRendererEvents {
    @Shadow @Final private Minecraft minecraft;
    @Shadow private int ticks;

    @Inject(method = "renderSectionLayer", at = @At("TAIL"))
    private void epicfight$afterSectionLayer(RenderType renderType, double x, double y, double z, Matrix4f frustumMatrix, Matrix4f projectionMatrix, CallbackInfo ci) {
        if (renderType == RenderType.tripwire() && RenderLevelStageEvent.BUS.hasListeners()) {
            LevelRenderer self = (LevelRenderer)(Object)this;
            this.minecraft.getProfiler().push("AFTER_TRIPWIRE_BLOCKS");
            RenderLevelStageEvent.BUS.post(new RenderLevelStageEvent(
                RenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS, self, new PoseStack(), frustumMatrix, projectionMatrix, this.ticks,
                this.minecraft.getTimer(), this.minecraft.gameRenderer.getMainCamera(), self.getFrustum()
            ));
            this.minecraft.getProfiler().pop();
        }
    }
}
