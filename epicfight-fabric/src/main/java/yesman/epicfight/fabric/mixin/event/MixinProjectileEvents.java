package yesman.epicfight.fabric.mixin.event;

import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.api.event.impl.VanillaEntityEventHooks;

/// NeoForge: `ProjectileImpactEvent`, posted by every projectile right before
/// `Projectile#hitTargetOrDeflectSelf` (or `onHit`) unless the hit is a miss. Cancelling it skips
/// the hit; `AbstractArrow` also stops checking further hits in that tick.
///
/// Hooked once in `hitTargetOrDeflectSelf` instead: a canceled impact returns a deflection that
/// does nothing, which skips the hit and makes `AbstractArrow` stop its loop the same way (the
/// other callers ignore the result).
@Mixin(Projectile.class)
public abstract class MixinProjectileEvents {
    @Unique
    private static final ProjectileDeflection EPICFIGHT_CANCELED_IMPACT = (projectile, entity, random) -> {};

    @Inject(method = "hitTargetOrDeflectSelf", at = @At("HEAD"), cancellable = true)
    private void epicfight$onImpact(HitResult hitResult, CallbackInfoReturnable<ProjectileDeflection> cir) {
        if (hitResult.getType() != HitResult.Type.MISS && VanillaEntityEventHooks.onProjectileImpacts(hitResult, (Projectile)(Object)this)) {
            cir.setReturnValue(EPICFIGHT_CANCELED_IMPACT);
        }
    }
}
