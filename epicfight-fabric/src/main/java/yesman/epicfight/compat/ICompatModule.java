package yesman.epicfight.compat;

import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.main.EpicFightSharedConstants;

import java.lang.reflect.Constructor;

/// Optional integration with another mod, loaded only when that mod is present.
///
/// Upstream modules receive the NeoForge mod and game event buses; on Fabric they register their
/// callbacks directly in [#onCommonInit()] and [#onClientInit()].
public interface ICompatModule {
	static void loadCompatModule(Class<? extends ICompatModule> compatModule) {
		try {
			Constructor<? extends ICompatModule> constructor = compatModule.getConstructor();
			ICompatModule compatModuleInstance = constructor.newInstance();
			compatModuleInstance.onCommonInit();
			
			if (EpicFightSharedConstants.isPhysicalClient()) {
				compatModuleInstance.onClientInit();
			}

            EpicFightMod.LOGGER.info("Loaded mod compatibility module: {}", compatModule.getSimpleName());
		} catch (Exception e) {
            EpicFightMod.LOGGER.error("Failed to load mod compatibility module: {}", e.getMessage());
			e.printStackTrace();
		}
	}
	
	default void onCommonInit() {}
	
	default void onClientInit() {}
}
