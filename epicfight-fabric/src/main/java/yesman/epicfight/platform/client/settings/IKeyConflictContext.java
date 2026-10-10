package yesman.epicfight.platform.client.settings;

/// Port of NeoForge's `IKeyConflictContext`: decides when a key mapping is active, so mappings
/// that are never active at the same time (GUI vs. in game) can share a key.
public interface IKeyConflictContext {
    boolean isActive();

    boolean conflicts(IKeyConflictContext other);
}
