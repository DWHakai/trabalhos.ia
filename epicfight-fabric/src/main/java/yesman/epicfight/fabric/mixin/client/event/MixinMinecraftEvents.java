package yesman.epicfight.fabric.mixin.client.event;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.api.client.event.impl.VanillaGeneralClientEventHooks;
import yesman.epicfight.client.world.util.FakeLevel;
import yesman.epicfight.platform.client.event.InputEvent;
import yesman.epicfight.platform.client.event.RenderFrameEvent;

/// NeoForge hooks in `Minecraft`:
/// - `RenderFrameEvent.Pre`/`Post` around `GameRenderer#render`;
/// - `InputEvent.InteractionKeyMappingTriggered` (`ClientHooks#onClickInput`) in `continueAttack`,
///   `startAttack`, `startUseItem` and `pickBlock`, with the same cancel/swing handling;
/// - `ClientPlayerNetworkEvent.LoggingOut` in `disconnect`;
/// - `LevelEvent.Unload` of the client level in `setLevel`, `disconnect` and `clearClientLevel`.
@Mixin(Minecraft.class)
public abstract class MixinMinecraftEvents {
    @Shadow @Final public Options options;
    @Shadow @Nullable public LocalPlayer player;
    @Shadow @Nullable public HitResult hitResult;
    @Shadow @Final public ParticleEngine particleEngine;
    @Shadow @Nullable public ClientLevel level;
    @Shadow @Final private DeltaTracker.Timer timer;

    @Unique
    private InputEvent.InteractionKeyMappingTriggered epicfight$attackEvent;
    @Unique
    private InputEvent.InteractionKeyMappingTriggered epicfight$useItemEvent;

    @Unique
    private static InputEvent.InteractionKeyMappingTriggered epicfight$onClickInput(int button, net.minecraft.client.KeyMapping keyMapping, InteractionHand hand) {
        return InputEvent.InteractionKeyMappingTriggered.BUS.post(new InputEvent.InteractionKeyMappingTriggered(button, keyMapping, hand));
    }

    // RenderFrameEvent

    @Inject(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;render(Lnet/minecraft/client/DeltaTracker;Z)V"))
    private void epicfight$renderFramePre(boolean renderLevel, CallbackInfo ci) {
        RenderFrameEvent.Pre.BUS.post(new RenderFrameEvent.Pre(this.timer));
    }

    @Inject(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;render(Lnet/minecraft/client/DeltaTracker;Z)V", shift = At.Shift.AFTER))
    private void epicfight$renderFramePost(boolean renderLevel, CallbackInfo ci) {
        RenderFrameEvent.Post.BUS.post(new RenderFrameEvent.Post(this.timer));
    }

    // continueAttack: fired before destroying the targeted block further

    @WrapOperation(method = "continueAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;continueDestroyBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)Z"))
    private boolean epicfight$continueAttack(MultiPlayerGameMode gameMode, BlockPos blockPos, Direction direction, Operation<Boolean> original) {
        InputEvent.InteractionKeyMappingTriggered inputEvent = epicfight$onClickInput(0, this.options.keyAttack, InteractionHand.MAIN_HAND);

        if (inputEvent.isCanceled()) {
            if (inputEvent.shouldSwingHand()) {
                this.particleEngine.crack(blockPos, direction);
                this.player.swing(InteractionHand.MAIN_HAND);
            }

            return false;
        }

        // Vanilla cracks and swings when this returns true
        return original.call(gameMode, blockPos, direction) && inputEvent.shouldSwingHand();
    }

    // startAttack: fired before acting on the hit result

    @Inject(method = "startAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/HitResult;getType()Lnet/minecraft/world/phys/HitResult$Type;"), cancellable = true)
    private void epicfight$startAttack(CallbackInfoReturnable<Boolean> cir) {
        InputEvent.InteractionKeyMappingTriggered inputEvent = epicfight$onClickInput(0, this.options.keyAttack, InteractionHand.MAIN_HAND);
        this.epicfight$attackEvent = inputEvent;

        if (inputEvent.isCanceled()) {
            if (inputEvent.shouldSwingHand()) {
                this.player.swing(InteractionHand.MAIN_HAND);
            }

            cir.setReturnValue(false);
        }
    }

    @WrapWithCondition(method = "startAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;swing(Lnet/minecraft/world/InteractionHand;)V"))
    private boolean epicfight$startAttackSwing(LocalPlayer player, InteractionHand hand) {
        return this.epicfight$attackEvent == null || this.epicfight$attackEvent.shouldSwingHand();
    }

    // startUseItem: fired for each hand before it is used

    @Inject(method = "startUseItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getItemInHand(Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;"), cancellable = true)
    private void epicfight$startUseItem(CallbackInfo ci, @Local InteractionHand hand) {
        InputEvent.InteractionKeyMappingTriggered inputEvent = epicfight$onClickInput(1, this.options.keyUse, hand);
        this.epicfight$useItemEvent = inputEvent;

        if (inputEvent.isCanceled()) {
            if (inputEvent.shouldSwingHand()) {
                this.player.swing(hand);
            }

            ci.cancel();
        }
    }

    @WrapWithCondition(method = "startUseItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;swing(Lnet/minecraft/world/InteractionHand;)V", ordinal = 0))
    private boolean epicfight$interactSwing(LocalPlayer player, InteractionHand hand) {
        return this.epicfight$useItemEvent == null || this.epicfight$useItemEvent.shouldSwingHand();
    }

    @WrapWithCondition(method = "startUseItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;swing(Lnet/minecraft/world/InteractionHand;)V", ordinal = 1))
    private boolean epicfight$useItemOnSwing(LocalPlayer player, InteractionHand hand) {
        return this.epicfight$useItemEvent == null || this.epicfight$useItemEvent.shouldSwingHand();
    }

    // pickBlock

    @Inject(method = "pickBlock", at = @At("HEAD"), cancellable = true)
    private void epicfight$pickBlock(CallbackInfo ci) {
        if (this.hitResult != null && this.hitResult.getType() != HitResult.Type.MISS) {
            if (epicfight$onClickInput(2, this.options.keyPickItem, InteractionHand.MAIN_HAND).isCanceled()) {
                ci.cancel();
            }
        }
    }

    // ClientPlayerNetworkEvent.LoggingOut (ClientHooks#firePlayerLogout)

    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screens/Screen;Z)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;resetData()V", shift = At.Shift.AFTER))
    private void epicfight$loggingOut(CallbackInfo ci) {
        if (this.player != null) {
            VanillaGeneralClientEventHooks.onPlayerLoggedOut(this.player);
        }
    }

    // LevelEvent.Unload (upstream NeoForgeWorldEvent.WorldEventsClient#unloadLevel)

    @Inject(method = "setLevel", at = @At("HEAD"))
    private void epicfight$unloadLevel(CallbackInfo ci) {
        if (this.level != null) {
            FakeLevel.unloadFakeLevel();
        }
    }

    @Inject(method = {"disconnect(Lnet/minecraft/client/gui/screens/Screen;Z)V", "clearClientLevel"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;onDisconnected()V"))
    private void epicfight$unloadLevelOnDisconnect(CallbackInfo ci) {
        FakeLevel.unloadFakeLevel();
    }
}
