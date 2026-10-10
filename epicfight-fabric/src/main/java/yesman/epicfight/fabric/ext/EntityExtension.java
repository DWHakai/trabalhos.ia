package yesman.epicfight.fabric.ext;

/// NeoForge's `IEntityExtension` defaults used by Epic Fight, injected into `Entity`.
/// No vanilla entity overrides them, so the defaults are the real behaviour.
public interface EntityExtension {
    /// If the rider should be rendered sitting.
    default boolean shouldRiderSit() {
        return true;
    }

    /// If the rider can interact (attack) with this entity while riding it.
    default boolean canRiderInteract() {
        return false;
    }
}
