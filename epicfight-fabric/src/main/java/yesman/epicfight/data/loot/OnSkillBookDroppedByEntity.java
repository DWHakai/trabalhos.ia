package yesman.epicfight.data.loot;

import com.google.common.collect.Maps;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.api.event.types.registry.RegisterMobSkillBookLootTableEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/// Fabric port: NeoForge global loot modifier `epicfight:skillbook_modifier` (condition
/// `killed_by_player`). Fabric has no global loot modifiers, so `MixinLootTableModifiers` calls
/// [#apply] where NeoForge runs `CommonHooks#modifyLoot`.
public class OnSkillBookDroppedByEntity {
	private static final LootItemCondition KILLED_BY_PLAYER = LootItemKilledByPlayerCondition.killedByPlayer().build();
	private static final Map<EntityType<?>, LootTable> SKILL_LOOT_TABLE = Maps.newHashMap();

	public static void registerEntitySkillLootTable() {
		Map<EntityType<?>, LootTable.Builder> builders = new HashMap<> ();
        RegisterMobSkillBookLootTableEvent skillBookLootTableRegistryEvent = new RegisterMobSkillBookLootTableEvent(builders);
        EpicFightEventHooks.Registry.SKILLBOOK_LOOT_TABLE.post(skillBookLootTableRegistryEvent);
		SKILL_LOOT_TABLE.clear();

		builders.forEach((k, v) -> {
			SKILL_LOOT_TABLE.put(k, v.build());
		});
	}

	/// Adds the skill book drops of the entity in the loot context to the generated loot
	public static void apply(LootContext context, Consumer<ItemStack> generatedLoot) {
		Entity entity = context.getParamOrNull(LootContextParams.THIS_ENTITY);

		if (entity != null && SKILL_LOOT_TABLE.containsKey(entity.getType()) && KILLED_BY_PLAYER.test(context)) {
			// Call getRandomItemsRaw to avoid recursive call of global loot modifier
			SKILL_LOOT_TABLE.get(entity.getType()).getRandomItemsRaw(context, generatedLoot);
		}
	}
}
