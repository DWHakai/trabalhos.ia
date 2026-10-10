package yesman.epicfight.network;

import net.minecraft.world.entity.player.Player;

/// Same shape as NeoForge's `IPayloadContext`, as far as Epic Fight uses it: the handlers only
/// need the player on the receiving side. Handlers run on the main thread on both sides.
@FunctionalInterface
public interface IPayloadContext {
    Player player();
}
