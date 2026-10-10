package yesman.epicfight.fabric.ext;

import com.mojang.blaze3d.platform.InputConstants;
import yesman.epicfight.platform.client.settings.IKeyConflictContext;

/// NeoForge's `IKeyMappingExtension` (the parts Epic Fight uses), injected into `KeyMapping` by Loom
/// and implemented by `MixinKeyMapping`.
public interface KeyMappingExtension {
    default InputConstants.Key getKey() {
        throw new AssertionError("implemented by mixin");
    }

    default IKeyConflictContext getKeyConflictContext() {
        throw new AssertionError("implemented by mixin");
    }

    default void setKeyConflictContext(IKeyConflictContext keyConflictContext) {
        throw new AssertionError("implemented by mixin");
    }

    /// Returns true when the given key is this mapping's key and the conflict context is active.
    default boolean isActiveAndMatches(InputConstants.Key keyCode) {
        return keyCode != InputConstants.UNKNOWN && keyCode.equals(this.getKey()) && this.getKeyConflictContext().isActive();
    }

    default boolean isConflictContextAndModifierActive() {
        return this.getKeyConflictContext().isActive();
    }
}
