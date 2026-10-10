package yesman.epicfight.fabric;

import yesman.epicfight.world.capabilities.provider.AttachmentEntityPatchProvider;

/// Implemented by every entity through a mixin
public interface EntityPatchHolder {
    AttachmentEntityPatchProvider epicfight$getEntityPatchProvider();
}
