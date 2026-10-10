package yesman.epicfight.platform.event;

import net.minecraft.world.entity.LivingEntity;

/// NeoForge `LivingEvent.LivingJumpEvent`, fired at the end of `LivingEntity#jumpFromGround`
/// on both sides.
public class LivingJumpEvent {
    public static final ListenerList<LivingJumpEvent> BUS = new ListenerList<>();

    private final LivingEntity entity;

    public LivingJumpEvent(LivingEntity entity) {
        this.entity = entity;
    }

    public LivingEntity getEntity() {
        return this.entity;
    }
}
