package yesman.epicfight.fabric.mixin.event;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.api.event.impl.VanillaPlayerEventHooks;

/// NeoForge: `ArrowLooseEvent` in `BowItem#releaseUsing`, once the charge time is known and the
/// player has ammo.
@Mixin(BowItem.class)
public abstract class MixinBowItemEvents {
    @Inject(method = "releaseUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/BowItem;getPowerForTime(I)F"))
    private void epicfight$onArrowLoose(ItemStack stack, Level level, LivingEntity livingEntity, int timeCharged, CallbackInfo ci) {
        VanillaPlayerEventHooks.onLooseArrow((Player)livingEntity);
    }
}
