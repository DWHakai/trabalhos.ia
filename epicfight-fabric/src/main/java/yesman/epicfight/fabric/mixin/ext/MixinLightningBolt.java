package yesman.epicfight.fabric.mixin.ext;

import net.minecraft.world.entity.LightningBolt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import yesman.epicfight.fabric.ext.LightningBoltExtension;

@Mixin(LightningBolt.class)
public abstract class MixinLightningBolt implements LightningBoltExtension {
    @Unique
    private float epicfight$damage = 5.0F;

    @Override
    public void setDamage(float damage) {
        this.epicfight$damage = damage;
    }

    @Override
    public float getDamage() {
        return this.epicfight$damage;
    }
}
