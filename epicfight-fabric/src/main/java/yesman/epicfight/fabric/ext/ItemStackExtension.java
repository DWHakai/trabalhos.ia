package yesman.epicfight.fabric.ext;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import yesman.epicfight.fabric.ItemAttributeHooks;

/// NeoForge's `IItemStackExtension` methods that Epic Fight uses, with NeoForge's default
/// behaviour. Injected into `ItemStack`.
public interface ItemStackExtension {
    default int getEnchantmentLevel(Holder<Enchantment> enchantment) {
        return EnchantmentHelper.getItemEnchantmentLevel(enchantment, (ItemStack)(Object)this);
    }

    /// The stack's attribute modifiers after NeoForge's `ItemAttributeModifierEvent`, see
    /// [ItemAttributeHooks].
    default ItemAttributeModifiers getAttributeModifiers() {
        return ItemAttributeHooks.getAttributeModifiers((ItemStack)(Object)this);
    }

    /// NeoForge: `IItemExtension#getEquipmentSlot`, which returns null unless an item overrides it.
    @Nullable
    default EquipmentSlot getEquipmentSlot() {
        return null;
    }

    /// NeoForge: `IItemExtension#getEntityLifespan`, 6000 ticks unless an item overrides it.
    default int getEntityLifespan(Level level) {
        return 6000;
    }
}
