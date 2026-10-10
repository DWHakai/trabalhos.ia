package yesman.epicfight.fabric.mixin.event;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.api.event.impl.VanillaPlayerEventHooks;

/// NeoForge: `ArrowLooseEvent` when a player shoots a crossbow, on the server.
@Mixin(CrossbowItem.class)
public abstract class MixinCrossbowItemEvents {
    @Inject(method = "performShooting", at = @At("HEAD"))
    private void epicfight$onArrowLoose(Level level, LivingEntity shooter, InteractionHand hand, ItemStack weapon, float velocity, float inaccuracy, @Nullable LivingEntity target, CallbackInfo ci) {
        if (level instanceof ServerLevel && shooter instanceof Player player) {
            VanillaPlayerEventHooks.onLooseArrow(player);
        }
    }
}
