package yesman.epicfight.fabric.mixin.client.event;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleRenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/// NeoForge renders every particle render type present in the particle map (custom ones included),
/// vanilla only the ones in `RENDER_ORDER`. Epic Fight's `MixinParticleEngine` already makes the
/// map a TreeMap ordered like `RENDER_ORDER` with custom types after, so iterating its keys keeps
/// the vanilla order.
@Mixin(ParticleEngine.class)
public abstract class MixinParticleEngineRender {
    @Shadow private Map<ParticleRenderType, Queue<Particle>> particles;

    @Redirect(method = "render", at = @At(value = "FIELD", target = "Lnet/minecraft/client/particle/ParticleEngine;RENDER_ORDER:Ljava/util/List;"))
    private List<ParticleRenderType> epicfight$allRenderTypes() {
        List<ParticleRenderType> renderTypes = new ArrayList<>(this.particles.keySet());
        renderTypes.remove(ParticleRenderType.NO_RENDER);
        return renderTypes;
    }
}
