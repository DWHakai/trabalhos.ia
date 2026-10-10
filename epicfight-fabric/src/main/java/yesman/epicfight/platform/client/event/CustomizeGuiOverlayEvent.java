package yesman.epicfight.platform.client.event;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.LerpingBossEvent;
import yesman.epicfight.platform.event.CancellableEvent;
import yesman.epicfight.platform.event.ListenerList;

/// NeoForge `CustomizeGuiOverlayEvent` (only `BossEventProgress` is used by Epic Fight).
public abstract class CustomizeGuiOverlayEvent extends CancellableEvent {
    private final Window window;
    private final GuiGraphics guiGraphics;

    protected CustomizeGuiOverlayEvent(Window window, GuiGraphics guiGraphics) {
        this.window = window;
        this.guiGraphics = guiGraphics;
    }

    public Window getWindow() {
        return this.window;
    }

    public GuiGraphics getGuiGraphics() {
        return this.guiGraphics;
    }

    /// Fired in `BossHealthOverlay#render` before each boss bar; canceling skips the bar and its name.
    public static class BossEventProgress extends CustomizeGuiOverlayEvent {
        public static final ListenerList<BossEventProgress> BUS = new ListenerList<>();

        private final LerpingBossEvent bossEvent;
        private final int x;
        private final int y;
        private int increment;

        public BossEventProgress(Window window, GuiGraphics guiGraphics, LerpingBossEvent bossEvent, int x, int y, int increment) {
            super(window, guiGraphics);
            this.bossEvent = bossEvent;
            this.x = x;
            this.y = y;
            this.increment = increment;
        }

        public LerpingBossEvent getBossEvent() {
            return this.bossEvent;
        }

        public int getX() {
            return this.x;
        }

        public int getY() {
            return this.y;
        }

        public int getIncrement() {
            return this.increment;
        }

        public void setIncrement(int increment) {
            this.increment = increment;
        }
    }
}
