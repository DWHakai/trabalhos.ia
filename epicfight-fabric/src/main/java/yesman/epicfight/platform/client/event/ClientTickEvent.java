package yesman.epicfight.platform.client.event;

import yesman.epicfight.platform.event.ListenerList;

/// NeoForge `ClientTickEvent`, fired from Fabric's `ClientTickEvents` (start/end of `Minecraft#tick`).
public abstract class ClientTickEvent {
    public static class Pre extends ClientTickEvent {
        public static final ListenerList<Pre> BUS = new ListenerList<>();
        public static final Pre INSTANCE = new Pre();
    }

    public static class Post extends ClientTickEvent {
        public static final ListenerList<Post> BUS = new ListenerList<>();
        public static final Post INSTANCE = new Post();
    }
}
