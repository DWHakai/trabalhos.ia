package yesman.epicfight.registry.entries;

import net.minecraft.world.entity.Entity;
import yesman.epicfight.fabric.EntityPatchHolder;
import yesman.epicfight.world.capabilities.provider.AttachmentEntityPatchProvider;

/// Upstream registers a NeoForge data attachment that holds the entity patch. On Fabric the
/// holder is a field added to [Entity] by `yesman.epicfight.fabric.mixin.MixinEntityPatchHolder`,
/// created lazily on first access exactly like a non-serialized NeoForge attachment.
public final class EpicFightAttachmentTypes {
	private EpicFightAttachmentTypes() {}

	public static AttachmentEntityPatchProvider getEntityPatchProvider(Entity entity) {
		return ((EntityPatchHolder)entity).epicfight$getEntityPatchProvider();
	}
}
