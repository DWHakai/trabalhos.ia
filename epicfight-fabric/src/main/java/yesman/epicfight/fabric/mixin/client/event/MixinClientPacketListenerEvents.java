package yesman.epicfight.fabric.mixin.client.event;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.api.client.event.impl.VanillaGeneralClientEventHooks;

/// NeoForge: `ClientPlayerNetworkEvent.LoggingIn` and `.Clone`, fired after the new local player
/// was reset and before it is added to the level (`ClientHooks#firePlayerLogin/firePlayerRespawn`).
@Mixin(ClientPacketListener.class)
public abstract class MixinClientPacketListenerEvents {
    @Inject(method = "handleLogin", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;resetPos()V", shift = At.Shift.AFTER))
    private void epicfight$loggingIn(ClientboundLoginPacket packet, CallbackInfo ci) {
        VanillaGeneralClientEventHooks.onPlayerLoggedIn(Minecraft.getInstance().player);
    }

    @Inject(method = "handleRespawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;resetPos()V", shift = At.Shift.AFTER))
    private void epicfight$clonePlayer(ClientboundRespawnPacket packet, CallbackInfo ci, @Local(ordinal = 0) LocalPlayer oldPlayer, @Local(ordinal = 1) LocalPlayer newPlayer) {
        VanillaGeneralClientEventHooks.onClonedInClient(oldPlayer, newPlayer);
    }
}
