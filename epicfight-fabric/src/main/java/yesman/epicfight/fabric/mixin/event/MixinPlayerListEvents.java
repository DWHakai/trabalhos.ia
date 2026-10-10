package yesman.epicfight.fabric.mixin.event;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.storage.PlayerDataStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.api.event.impl.VanillaPlayerEventHooks;

import java.util.Optional;

/// NeoForge: `PlayerEvent.LoadFromFile`, posted when the data of a player was read, from the
/// level data of the singleplayer owner or from the player data folder
/// (`PlayerDataStorage#load`). Both end in `PlayerList#load`.
@Mixin(PlayerList.class)
public abstract class MixinPlayerListEvents {
    @Shadow
    @Final
    private PlayerDataStorage playerIo;

    @Inject(method = "load", at = @At("RETURN"))
    private void epicfight$onLoad(ServerPlayer player, CallbackInfoReturnable<Optional<CompoundTag>> cir) {
        if (cir.getReturnValue().isPresent()) {
            VanillaPlayerEventHooks.onLoad(player, this.playerIo.playerDir, player.getStringUUID());
        }
    }
}
