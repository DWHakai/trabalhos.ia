package yesman.epicfight.fabric.ext;

/// NeoForge gives `LightningBolt` a configurable damage (vanilla hardcodes 5 in `Entity#thunderHit`).
public interface LightningBoltExtension {
    default void setDamage(float damage) {
        throw new AssertionError("implemented by mixin");
    }

    default float getDamage() {
        throw new AssertionError("implemented by mixin");
    }
}
