package yesman.epicfight.fabric.ext;

/// NeoForge's injected stencil control on `RenderTarget`, implemented by `MixinRenderTarget`.
public interface RenderTargetExtension {
    /// Attempts to enable 8 bits of stencil buffer on this frame buffer (render thread only)
    default void enableStencil() {
        throw new AssertionError("implemented by mixin");
    }

    default boolean isStencilEnabled() {
        throw new AssertionError("implemented by mixin");
    }
}
