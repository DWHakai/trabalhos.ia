package yesman.epicfight.platform.fabric.client;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import org.jetbrains.annotations.NotNull;
import yesman.epicfight.platform.client.KeyMappingRegistrar;

/// Fabric registers key mappings immediately, so there is no pending list like on NeoForge.
public class FabricKeyMappingRegistrar implements KeyMappingRegistrar {
    @Override
    public void registerKeyMapping(@NotNull final KeyMapping keyMapping) {
        KeyBindingHelper.registerKeyBinding(keyMapping);
    }
}
