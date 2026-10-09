package yesman.epicfight.config;

import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import yesman.epicfight.main.EpicFightMod;

public class ServerConfig {
	private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
	public static final ModConfigSpec.BooleanValue ALLOW_CUSTOM_ANIMATIONS = BUILDER.define("allow_custom_animations", false);
	public static final ModConfigSpec SPEC = BUILDER.build();
	
	public static boolean allowCustomAnimations;
    public static void onLoad(final ModConfig config) {
		if (config.getType() != ModConfig.Type.SERVER) {
			return;
		}
		
		allowCustomAnimations = ALLOW_CUSTOM_ANIMATIONS.get();
	}
}
