package yesman.epicfight.fabric.mixin.client;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL30;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import yesman.epicfight.fabric.ext.RenderTargetExtension;

import java.nio.IntBuffer;

/// NeoForge's `RenderTarget` patch: an optional 8 bit stencil buffer, packed with the depth texture
/// (NeoForge's default separate DEPTH and STENCIL attachments of the same texture).
@Mixin(RenderTarget.class)
public abstract class MixinRenderTarget implements RenderTargetExtension {
    @Shadow public int viewWidth;
    @Shadow public int viewHeight;
    @Shadow protected int depthBufferId;

    @Shadow public abstract void resize(int width, int height, boolean clearError);

    @Unique
    private boolean epicfight$stencilEnabled;

    @WrapWithCondition(method = "createBuffers", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;_texImage2D(IIIIIIIILjava/nio/IntBuffer;)V"))
    private boolean epicfight$depthStencilTexture(int target, int level, int internalFormat, int width, int height, int border, int format, int type, IntBuffer pixels) {
        // The depth texture is the only one created with the DEPTH_COMPONENT format
        if (this.epicfight$stencilEnabled && format == 6402) {
            GlStateManager._texImage2D(target, level, GL30.GL_DEPTH32F_STENCIL8, width, height, border, GL30.GL_DEPTH_STENCIL, GL30.GL_FLOAT_32_UNSIGNED_INT_24_8_REV, null);
            return false;
        }

        return true;
    }

    @WrapWithCondition(method = "createBuffers", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;_glFramebufferTexture2D(IIIII)V"))
    private boolean epicfight$stencilAttachment(int target, int attachment, int texTarget, int texture, int level) {
        if (this.epicfight$stencilEnabled && attachment == GL30.GL_DEPTH_ATTACHMENT) {
            GlStateManager._glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, texTarget, this.depthBufferId, 0);
            GlStateManager._glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_STENCIL_ATTACHMENT, texTarget, this.depthBufferId, 0);
            return false;
        }

        return true;
    }

    @Override
    public void enableStencil() {
        if (this.epicfight$stencilEnabled) {
            return;
        }

        this.epicfight$stencilEnabled = true;
        this.resize(this.viewWidth, this.viewHeight, Minecraft.ON_OSX);
    }

    @Override
    public boolean isStencilEnabled() {
        return this.epicfight$stencilEnabled;
    }
}
