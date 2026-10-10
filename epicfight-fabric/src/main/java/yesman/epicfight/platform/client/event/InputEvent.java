package yesman.epicfight.platform.client.event;

import net.minecraft.client.KeyMapping;
import net.minecraft.world.InteractionHand;
import yesman.epicfight.platform.event.CancellableEvent;
import yesman.epicfight.platform.event.ListenerList;

/// NeoForge `InputEvent` (the two sub-events Epic Fight uses).
public abstract class InputEvent extends CancellableEvent {
    /// Fired in `MouseHandler#onScroll` before the hotbar is scrolled; canceling keeps the slot.
    public static class MouseScrollingEvent extends InputEvent {
        public static final ListenerList<MouseScrollingEvent> BUS = new ListenerList<>();

        private final double scrollDeltaX;
        private final double scrollDeltaY;
        private final double mouseX;
        private final double mouseY;
        private final boolean leftDown;
        private final boolean middleDown;
        private final boolean rightDown;

        public MouseScrollingEvent(double scrollDeltaX, double scrollDeltaY, boolean leftDown, boolean middleDown, boolean rightDown, double mouseX, double mouseY) {
            this.scrollDeltaX = scrollDeltaX;
            this.scrollDeltaY = scrollDeltaY;
            this.leftDown = leftDown;
            this.middleDown = middleDown;
            this.rightDown = rightDown;
            this.mouseX = mouseX;
            this.mouseY = mouseY;
        }

        public double getScrollDeltaX() {
            return this.scrollDeltaX;
        }

        public double getScrollDeltaY() {
            return this.scrollDeltaY;
        }

        public boolean isLeftDown() {
            return this.leftDown;
        }

        public boolean isRightDown() {
            return this.rightDown;
        }

        public boolean isMiddleDown() {
            return this.middleDown;
        }

        public double getMouseX() {
            return this.mouseX;
        }

        public double getMouseY() {
            return this.mouseY;
        }
    }

    /// Fired when the attack, use or pick block key mapping triggers its action
    /// (`Minecraft#startAttack`, `#continueAttack`, `#startUseItem`, `#pickBlock`).
    /// Canceling stops the action; `setSwingHand(false)` also stops the arm swing.
    public static class InteractionKeyMappingTriggered extends InputEvent {
        public static final ListenerList<InteractionKeyMappingTriggered> BUS = new ListenerList<>();

        private final int button;
        private final KeyMapping keyMapping;
        private final InteractionHand hand;
        private boolean handSwing = true;

        public InteractionKeyMappingTriggered(int button, KeyMapping keyMapping, InteractionHand hand) {
            this.button = button;
            this.keyMapping = keyMapping;
            this.hand = hand;
        }

        public void setSwingHand(boolean value) {
            this.handSwing = value;
        }

        public boolean shouldSwingHand() {
            return this.handSwing;
        }

        public InteractionHand getHand() {
            return this.hand;
        }

        public boolean isAttack() {
            return this.button == 0;
        }

        public boolean isUseItem() {
            return this.button == 1;
        }

        public boolean isPickBlock() {
            return this.button == 2;
        }

        public KeyMapping getKeyMapping() {
            return this.keyMapping;
        }
    }
}
