package yesman.epicfight.fabric.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import yesman.epicfight.fabric.client.model.SeparateTransformsModel;

/// NeoForge's `BakedModel#applyTransform` lets `neoforge:separate_transforms` models draw another
/// model per display context. [SeparateTransformsModel] picks it here, before the transforms and
/// the quads of the model are used.
@Mixin(ItemRenderer.class)
public abstract class MixinItemRendererSeparateTransforms {
    @ModifyVariable(method = "render", at = @At("HEAD"), argsOnly = true)
    private BakedModel epicfight$separateTransforms(BakedModel model, @Local(argsOnly = true) ItemDisplayContext displayContext) {
        return model instanceof SeparateTransformsModel.Baked separateTransforms ? separateTransforms.forDisplayContext(displayContext) : model;
    }
}
