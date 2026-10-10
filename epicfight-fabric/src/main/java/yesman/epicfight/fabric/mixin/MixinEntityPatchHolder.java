package yesman.epicfight.fabric.mixin;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import yesman.epicfight.fabric.EntityPatchHolder;
import yesman.epicfight.world.capabilities.provider.AttachmentEntityPatchProvider;

@Mixin(Entity.class)
public abstract class MixinEntityPatchHolder implements EntityPatchHolder {
    @Unique
    private AttachmentEntityPatchProvider epicfight$entityPatchProvider;

    @Override
    public AttachmentEntityPatchProvider epicfight$getEntityPatchProvider() {
        if (this.epicfight$entityPatchProvider == null) {
            this.epicfight$entityPatchProvider = new AttachmentEntityPatchProvider((Entity)(Object)this);
        }

        return this.epicfight$entityPatchProvider;
    }
}
