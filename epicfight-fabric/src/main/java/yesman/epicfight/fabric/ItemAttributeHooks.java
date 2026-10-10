package yesman.epicfight.fabric;

import com.google.common.collect.ImmutableList;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import yesman.epicfight.api.event.impl.VanillaItemEventHooks;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/// Replaces NeoForge's `IItemStackExtension#getAttributeModifiers` + `ItemAttributeModifierEvent`.
///
/// NeoForge makes `ItemStack#forEachModifier` (used for equipping, tooltips and everything else)
/// read the modifiers from that event; `MixinItemStack` does the same here, and Epic Fight's
/// listener ([VanillaItemEventHooks#onModifyItemAttribute]) is called directly.
public final class ItemAttributeHooks {
    public static ItemAttributeModifiers getAttributeModifiers(ItemStack stack) {
        ItemAttributeModifiers defaultModifiers = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);

        if (defaultModifiers.modifiers().isEmpty()) {
            defaultModifiers = stack.getItem().getDefaultAttributeModifiers();
        }

        // Same rules as NeoForge's ItemAttributeModifiersBuilder: modifiers are unique by
        // (attribute, modifier id), and adding an existing one is a no-op.
        List<ItemAttributeModifiers.Entry> entries = new ArrayList<>(defaultModifiers.modifiers());
        Set<Key> keys = new HashSet<>();

        for (ItemAttributeModifiers.Entry entry : entries) {
            keys.add(new Key(entry.attribute(), entry.modifier().id()));
        }

        int sizeBefore = entries.size();

        VanillaItemEventHooks.onModifyItemAttribute(stack, (attribute, modifier, slot) -> {
            if (keys.add(new Key(attribute, modifier.id()))) {
                entries.add(new ItemAttributeModifiers.Entry(attribute, modifier, slot));
            }
        });

        if (entries.size() == sizeBefore) {
            return defaultModifiers;
        }

        return new ItemAttributeModifiers(ImmutableList.copyOf(entries), defaultModifiers.showInTooltip());
    }

    private record Key(Holder<Attribute> attribute, ResourceLocation id) {
    }

    private ItemAttributeHooks() {
    }
}
