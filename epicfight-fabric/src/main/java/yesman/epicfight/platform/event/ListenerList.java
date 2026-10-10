package yesman.epicfight.platform.event;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/// Listeners of one platform event type: the Fabric port's stand-in for registering a listener of
/// that type on NeoForge's event bus.
///
/// Like NeoForge listeners registered without `receiveCanceled`, listeners stop receiving an
/// event once a previous listener canceled it.
public final class ListenerList<T> {
    private final List<Consumer<? super T>> listeners = new CopyOnWriteArrayList<>();

    public void addListener(Consumer<? super T> listener) {
        this.listeners.add(listener);
    }

    public boolean hasListeners() {
        return !this.listeners.isEmpty();
    }

    public <E extends T> E post(E event) {
        for (Consumer<? super T> listener : this.listeners) {
            if (event instanceof CancellableEvent cancellable && cancellable.isCanceled()) {
                break;
            }

            listener.accept(event);
        }

        return event;
    }
}
