package yesman.epicfight.fabric.mixin.ext;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import yesman.epicfight.fabric.ext.EntityExtension;

@Mixin(Entity.class)
public abstract class MixinEntityExtension implements EntityExtension {
    /// NeoForge: the lightning bolt's own damage instead of the hardcoded 5.
    @ModifyArg(
        method = "thunderHit",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"),
        index = 1
    )
    private float epicfight$lightningDamage(float damage, @com.llamalad7.mixinextras.sugar.Local(argsOnly = true) LightningBolt lightning) {
        return lightning.getDamage();
    }
}
