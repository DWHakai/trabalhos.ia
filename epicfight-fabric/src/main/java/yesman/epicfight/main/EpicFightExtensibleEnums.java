package yesman.epicfight.main;

import net.minecraft.world.item.Rarity;

/// Fabric port: NeoForge extends `Rarity` with `epicfight:unique` (green) through an `EnumProxy`.
/// Fabric has no enum extension, and vanilla builds the id map of `Rarity` in its static
/// initializer, so an appended constant could not be synced. Nothing in Epic Fight uses the
/// rarity, so `UNIQUE` falls back to [Rarity#EPIC].
public class EpicFightExtensibleEnums {
	public static void initExtensibleEnums() {
		EpicFightRairity.init();
	}

	public static class EpicFightRairity {
		public static Rarity UNIQUE;

		private static void init() {
			UNIQUE = Rarity.EPIC;
		}
	}
}
