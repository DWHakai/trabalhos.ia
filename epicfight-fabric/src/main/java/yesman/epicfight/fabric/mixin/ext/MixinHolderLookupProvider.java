package yesman.epicfight.fabric.mixin.ext;

import net.minecraft.core.HolderLookup;
import org.spongepowered.asm.mixin.Mixin;
import yesman.epicfight.fabric.ext.HolderLookupProviderExtension;

@Mixin(HolderLookup.Provider.class)
public interface MixinHolderLookupProvider extends HolderLookupProviderExtension {
}
