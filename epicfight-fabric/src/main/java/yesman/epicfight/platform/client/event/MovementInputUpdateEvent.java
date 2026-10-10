package yesman.epicfight.platform.client.event;

import net.minecraft.client.player.Input;
import net.minecraft.world.entity.player.Player;
import yesman.epicfight.platform.event.ListenerList;

/// NeoForge `MovementInputUpdateEvent`, fired in `LocalPlayer#aiStep` right after the movement
/// input was ticked.
public class MovementInputUpdateEvent {
    public static final ListenerList<MovementInputUpdateEvent> BUS = new ListenerList<>();

    private final Player player;
    private final Input input;

    public MovementInputUpdateEvent(Player player, Input input) {
        this.player = player;
        this.input = input;
    }

    public Player getEntity() {
        return this.player;
    }

    public Input getInput() {
        return this.input;
    }
}
