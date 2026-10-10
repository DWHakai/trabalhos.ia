package yesman.epicfight.platform.client.event;

import net.minecraft.client.Camera;
import yesman.epicfight.platform.event.ListenerList;

/// NeoForge `ViewportEvent` (only `ComputeCameraAngles` is used by Epic Fight).
public abstract class ViewportEvent {
    private final Camera camera;
    private final double partialTick;

    protected ViewportEvent(Camera camera, double partialTick) {
        this.camera = camera;
        this.partialTick = partialTick;
    }

    public Camera getCamera() {
        return this.camera;
    }

    public double getPartialTick() {
        return this.partialTick;
    }

    /// Fired in `Camera#setup` to alter the camera angles, roll included.
    public static class ComputeCameraAngles extends ViewportEvent {
        public static final ListenerList<ComputeCameraAngles> BUS = new ListenerList<>();

        private float yaw;
        private float pitch;
        private float roll;

        public ComputeCameraAngles(Camera camera, double partialTick, float yaw, float pitch, float roll) {
            super(camera, partialTick);
            this.yaw = yaw;
            this.pitch = pitch;
            this.roll = roll;
        }

        public float getYaw() {
            return this.yaw;
        }

        public void setYaw(float yaw) {
            this.yaw = yaw;
        }

        public float getPitch() {
            return this.pitch;
        }

        public void setPitch(float pitch) {
            this.pitch = pitch;
        }

        public float getRoll() {
            return this.roll;
        }

        public void setRoll(float roll) {
            this.roll = roll;
        }
    }
}
