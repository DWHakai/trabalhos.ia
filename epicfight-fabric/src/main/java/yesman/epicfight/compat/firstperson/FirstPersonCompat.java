package yesman.epicfight.compat.firstperson;

import dev.tr7zw.firstperson.api.ActivationHandler;
import dev.tr7zw.firstperson.api.FirstPersonAPI;
import yesman.epicfight.compat.ICompatModule;
import yesman.epicfight.config.ClientConfig;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

public class FirstPersonCompat implements ICompatModule {
	@Override
	public void onClientInit() {
		FirstPersonAPI.getActivationHandlers().add(new ActivationHandler() {
			public boolean preventFirstperson() {
				PlayerPatch<?> playerpatch = EpicFightCapabilities.getCachedLocalPlayerPatch();;
				
				if (playerpatch != null && (playerpatch.getPlayerMode() == PlayerPatch.PlayerMode.EPICFIGHT || !ClientConfig.enableOriginalModel) && ClientConfig.enableAnimatedFirstPersonModel) {
					return true;
				}
				
				return false;
			}
		});
	}
}
