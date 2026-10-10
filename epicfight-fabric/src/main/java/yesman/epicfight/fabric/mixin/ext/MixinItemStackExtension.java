package yesman.epicfight.fabric.mixin.ext;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.fabric.ext.ItemStackExtension;

import java.util.function.BiConsumer;

/// NeoForge: `forEachModifier` reflects the modifiers after `ItemAttributeModifierEvent`
/// ([ItemStackExtension#getAttributeModifiers]) instead of the raw component.
@Mixin(ItemStack.class)
public abstract class MixinItemStackExtension implements ItemStackExtension {
    @Inject(method = "forEachModifier(Lnet/minecraft/world/entity/EquipmentSlotGroup;Ljava/util/function/BiConsumer;)V", at = @At("HEAD"), cancellable = true)
    private void epicfight$forEachModifier(EquipmentSlotGroup slotGroup, BiConsumer<Holder<Attribute>, AttributeModifier> action, CallbackInfo ci) {
        ItemStack self = (ItemStack)(Object)this;
        this.getAttributeModifiers().forEach(slotGroup, action);
        EnchantmentHelper.forEachModifier(self, slotGroup, action);
        ci.cancel();
    }

    @Inject(method = "forEachModifier(Lnet/minecraft/world/entity/EquipmentSlot;Ljava/util/function/BiConsumer;)V", at = @At("HEAD"), cancellable = true)
    private void epicfight$forEachModifierBySlot(EquipmentSlot slot, BiConsumer<Holder<Attribute>, AttributeModifier> action, CallbackInfo ci) {
        ItemStack self = (ItemStack)(Object)this;
        this.getAttributeModifiers().forEach(slot, action);
        EnchantmentHelper.forEachModifier(self, slot, action);
        ci.cancel();
    }
}
