package yesman.epicfight.platform.client.event;

import net.minecraft.world.level.Level;
import yesman.epicfight.platform.event.ListenerList;

/// NeoForge `LevelTickEvent` for the client level, fired from Fabric's `ClientTickEvents`.
public abstract class LevelTickEvent {
    private final Level level;

    protected LevelTickEvent(Level level) {
        this.level = level;
    }

    public Level getLevel() {
        return this.level;
    }

    public static class Post extends LevelTickEvent {
        public static final ListenerList<Post> BUS = new ListenerList<>();

        public Post(Level level) {
            super(level);
        }
    }
}
