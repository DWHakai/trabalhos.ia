package yesman.epicfight.platform.client.event;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import yesman.epicfight.platform.event.CancellableEvent;
import yesman.epicfight.platform.event.ListenerList;

/// NeoForge `RenderGuiEvent`, around the HUD layers in `Gui#render`.
public abstract class RenderGuiEvent extends CancellableEvent {
    private final GuiGraphics guiGraphics;
    private final DeltaTracker partialTick;

    protected RenderGuiEvent(GuiGraphics guiGraphics, DeltaTracker partialTick) {
        this.guiGraphics = guiGraphics;
        this.partialTick = partialTick;
    }

    public GuiGraphics getGuiGraphics() {
        return this.guiGraphics;
    }

    public DeltaTracker getPartialTick() {
        return this.partialTick;
    }

    public static class Pre extends RenderGuiEvent {
        public static final ListenerList<Pre> BUS = new ListenerList<>();

        public Pre(GuiGraphics guiGraphics, DeltaTracker partialTick) {
            super(guiGraphics, partialTick);
        }
    }

    public static class Post extends RenderGuiEvent {
        public static final ListenerList<Post> BUS = new ListenerList<>();

        public Post(GuiGraphics guiGraphics, DeltaTracker partialTick) {
            super(guiGraphics, partialTick);
        }
    }
}
