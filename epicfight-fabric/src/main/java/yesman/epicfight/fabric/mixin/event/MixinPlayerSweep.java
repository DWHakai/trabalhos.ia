package yesman.epicfight.fabric.mixin.event;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import yesman.epicfight.world.item.WeaponItem;

/// NeoForge makes the sweep attack of `Player#attack` check `ItemAbilities.SWORD_SWEEP` instead
/// of `instanceof SwordItem`, and Epic Fight's [WeaponItem] (not a `SwordItem`) answers yes to
/// the default sword actions. Same result here: weapon items sweep like swords in vanilla combat.
@Mixin(Player.class)
public abstract class MixinPlayerSweep {
    // Loom doesn't remap the classValue of a CONSTANT injection point, so the class is given with
    // its Mojang name (development) and its intermediary name (the built jar); one of them matches.
    // On an `instanceof` the CONSTANT point needs @WrapOperation: @ModifyExpressionValue would
    // receive the Class constant instead of the boolean result.
    @WrapOperation(
        method = "attack",
        at = {
            @At(value = "CONSTANT", args = "classValue=net/minecraft/world/item/SwordItem"),
            @At(value = "CONSTANT", args = "classValue=net/minecraft/class_1829")
        },
        require = 1,
        allow = 1
    )
    private boolean epicfight$canSweep(Object item, Operation<Boolean> original) {
        return original.call(item) || item instanceof WeaponItem;
    }
}
