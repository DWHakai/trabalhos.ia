package yesman.epicfight.client.events.engine;

/// Fabric port: engines register their listeners on the platform event lists
/// (`yesman.epicfight.platform.client.event`) instead of NeoForge event buses.
public interface IEventBasedEngine {
	public void gameEventBus();
	
	public void modEventBus();
	
	public static void init() {
		RenderEngine.getInstance().gameEventBus();
		RenderEngine.getInstance().modEventBus();
		ControlEngine.getInstance().gameEventBus();
		ControlEngine.getInstance().modEventBus();
	}
}
