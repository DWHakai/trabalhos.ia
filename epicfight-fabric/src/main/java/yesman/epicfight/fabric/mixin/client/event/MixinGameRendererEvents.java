package yesman.epicfight.fabric.mixin.client.event;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.platform.client.event.RegisterShadersEvent;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

/// NeoForge: `RegisterShadersEvent` after the vanilla core shaders were created in
/// `GameRenderer#reloadShaders` (still inside its try block, before they are installed).
@Mixin(GameRenderer.class)
public abstract class MixinGameRendererEvents {
    @Inject(method = "reloadShaders", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;loadBlurEffect(Lnet/minecraft/server/packs/resources/ResourceProvider;)V", shift = At.Shift.AFTER))
    private void epicfight$registerShaders(ResourceProvider resourceProvider, CallbackInfo ci, @Local(ordinal = 1) List<Pair<ShaderInstance, Consumer<ShaderInstance>>> shaders) {
        try {
            RegisterShadersEvent.post(new RegisterShadersEvent(resourceProvider, shaders));
        } catch (IOException e) {
            shaders.forEach(pair -> pair.getFirst().close());
            throw new RuntimeException("could not reload shaders", e);
        }
    }
}
