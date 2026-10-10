package yesman.epicfight.platform.client.event;

import yesman.epicfight.platform.event.CancellableEvent;
import yesman.epicfight.platform.event.ListenerList;

/// NeoForge `RenderHighlightEvent.Block`: canceling hides the block outline. Fired from Fabric's
/// `WorldRenderEvents.BEFORE_BLOCK_OUTLINE`.
public abstract class RenderHighlightEvent extends CancellableEvent {
    public static class Block extends RenderHighlightEvent {
        public static final ListenerList<Block> BUS = new ListenerList<>();
    }
}
