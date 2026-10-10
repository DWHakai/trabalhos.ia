package yesman.epicfight.fabric.mixin.event;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.platform.client.event.ItemTooltipEvent;

import java.util.List;

/// NeoForge: `ItemTooltipEvent` at the end of `ItemStack#getTooltipLines` (not for hidden tooltips).
/// Fabric API's `ItemTooltipCallback` has no player, which Epic Fight needs.
@Mixin(ItemStack.class)
public abstract class MixinItemStackTooltip {
    @Inject(method = "getTooltipLines", at = @At("RETURN"))
    private void epicfight$itemTooltip(Item.TooltipContext tooltipContext, Player player, TooltipFlag tooltipFlag, CallbackInfoReturnable<List<Component>> cir) {
        ItemStack self = (ItemStack)(Object)this;

        if (!tooltipFlag.isCreative() && self.has(DataComponents.HIDE_TOOLTIP)) {
            return;
        }

        ItemTooltipEvent.BUS.post(new ItemTooltipEvent(self, player, cir.getReturnValue(), tooltipFlag, tooltipContext));
    }
}
