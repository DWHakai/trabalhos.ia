package yesman.epicfight.platform.event;

/// Base of the platform events that NeoForge marks `ICancellableEvent`.
public abstract class CancellableEvent {
    private boolean canceled;

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }

    public boolean isCanceled() {
        return this.canceled;
    }
}
