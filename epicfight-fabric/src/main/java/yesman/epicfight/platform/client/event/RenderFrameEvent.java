package yesman.epicfight.platform.client.event;

import net.minecraft.client.DeltaTracker;
import yesman.epicfight.platform.event.ListenerList;

/// NeoForge `RenderFrameEvent`, around `GameRenderer#render` in `Minecraft#runTick`.
public abstract class RenderFrameEvent {
    private final DeltaTracker partialTick;

    protected RenderFrameEvent(DeltaTracker partialTick) {
        this.partialTick = partialTick;
    }

    public DeltaTracker getPartialTick() {
        return this.partialTick;
    }

    public static class Pre extends RenderFrameEvent {
        public static final ListenerList<Pre> BUS = new ListenerList<>();

        public Pre(DeltaTracker partialTick) {
            super(partialTick);
        }
    }

    public static class Post extends RenderFrameEvent {
        public static final ListenerList<Post> BUS = new ListenerList<>();

        public Post(DeltaTracker partialTick) {
            super(partialTick);
        }
    }
}
