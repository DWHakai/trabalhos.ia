package yesman.epicfight.fabric.mixin.client;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import yesman.epicfight.fabric.ext.LevelRendererExtension;

@Mixin(LevelRenderer.class)
public abstract class MixinLevelRendererExtension implements LevelRendererExtension {
    @Shadow private Frustum cullingFrustum;
    @Shadow private Frustum capturedFrustum;
    @Shadow private int ticks;

    @Override
    public Frustum getFrustum() {
        return this.capturedFrustum != null ? this.capturedFrustum : this.cullingFrustum;
    }

    @Override
    public int getTicks() {
        return this.ticks;
    }
}
