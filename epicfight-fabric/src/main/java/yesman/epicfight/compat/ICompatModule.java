package yesman.epicfight.compat;

import yesman.epicfight.main.EpicFightMod;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

/// Optional integration with another mod, loaded only when that mod is present.
///
/// Upstream modules receive the NeoForge mod and game event buses; on Fabric they register their
/// callbacks directly in [#onCommonInit()] and [#onClientInit()].
///
/// [#onCommonInit()] runs from the main entrypoint, [#onClientInit()] later from the client
/// entrypoint ([#initClientModules()]), where upstream's `FMLClientSetupEvent` listeners ran.
public interface ICompatModule {
	List<ICompatModule> LOADED_MODULES = new ArrayList<>();
	
	static void loadCompatModule(Class<? extends ICompatModule> compatModule) {
		try {
			Constructor<? extends ICompatModule> constructor = compatModule.getConstructor();
			ICompatModule compatModuleInstance = constructor.newInstance();
			compatModuleInstance.onCommonInit();
			LOADED_MODULES.add(compatModuleInstance);

            EpicFightMod.LOGGER.info("Loaded mod compatibility module: {}", compatModule.getSimpleName());
		} catch (Exception e) {
            EpicFightMod.LOGGER.error("Failed to load mod compatibility module: {}", e.getMessage());
			e.printStackTrace();
		}
	}
	
	static void initClientModules() {
		for (ICompatModule compatModule : LOADED_MODULES) {
			try {
				compatModule.onClientInit();
			} catch (Exception e) {
				EpicFightMod.LOGGER.error("Failed to initialize the client side of mod compatibility module {}: {}", compatModule.getClass().getSimpleName(), e.getMessage());
				e.printStackTrace();
			}
		}
	}
	
	default void onCommonInit() {}
	
	default void onClientInit() {}
}
