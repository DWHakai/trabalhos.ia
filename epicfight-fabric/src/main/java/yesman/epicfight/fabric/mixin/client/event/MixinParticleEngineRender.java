package yesman.epicfight.fabric.mixin.client.event;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleRenderType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/// NeoForge renders every particle render type present in the particle map (custom ones included),
/// vanilla only the ones in `RENDER_ORDER`. Epic Fight's `MixinParticleEngine` already makes the
/// map a TreeMap ordered like `RENDER_ORDER` with custom types after.
///
/// The list read by `render` is extended instead of replaced, so it chains with Iris, which
/// redirects the same read to render the opaque and the translucent particles in two phases. The
/// custom types go with the phase that has the translucent sheet (the only one without Iris).
@Mixin(ParticleEngine.class)
public abstract class MixinParticleEngineRender {
    @Shadow @Final private static List<ParticleRenderType> RENDER_ORDER;
    @Shadow private Map<ParticleRenderType, Queue<Particle>> particles;

    @ModifyExpressionValue(method = "render", at = @At(value = "FIELD", target = "Lnet/minecraft/client/particle/ParticleEngine;RENDER_ORDER:Ljava/util/List;"))
    private List<ParticleRenderType> epicfight$addCustomRenderTypes(List<ParticleRenderType> renderTypes) {
        if (!renderTypes.contains(ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT)) {
            return renderTypes;
        }

        List<ParticleRenderType> withCustomTypes = new ArrayList<>(renderTypes);

        for (ParticleRenderType renderType : this.particles.keySet()) {
            if (renderType != ParticleRenderType.NO_RENDER && !RENDER_ORDER.contains(renderType)) {
                withCustomTypes.add(renderType);
            }
        }

        return withCustomTypes;
    }
}
