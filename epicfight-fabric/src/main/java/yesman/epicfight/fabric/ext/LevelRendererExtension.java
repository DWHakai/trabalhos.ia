package yesman.epicfight.fabric.ext;

import net.minecraft.client.renderer.culling.Frustum;

/// NeoForge adds `LevelRenderer#getFrustum()` and `#getTicks()`.
public interface LevelRendererExtension {
    default Frustum getFrustum() {
        throw new AssertionError("implemented by mixin");
    }

    default int getTicks() {
        throw new AssertionError("implemented by mixin");
    }
}
