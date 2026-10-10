package yesman.epicfight.platform.client.settings;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;

/// Stands in for NeoForge's `KeyMapping(String, IKeyConflictContext, InputConstants.Type, int, String)`
/// constructor, which vanilla does not have.
public class ContextKeyMapping extends KeyMapping {
    public ContextKeyMapping(String description, IKeyConflictContext keyConflictContext, InputConstants.Type inputType, int keyCode, String category) {
        super(description, inputType, keyCode, category);
        this.setKeyConflictContext(keyConflictContext);
    }
}
