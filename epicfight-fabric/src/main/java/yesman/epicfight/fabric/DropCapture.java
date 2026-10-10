package yesman.epicfight.fabric;

import net.minecraft.world.entity.item.ItemEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/// NeoForge's `IEntityExtension#captureDrops`, implemented on `Entity` by `MixinEntityEvents`.
///
/// While a collection is set, the item entities from `Entity#spawnAtLocation(ItemStack, float)`
/// and `ServerPlayer#drop(ItemStack, boolean, boolean)` are added to it instead of the level.
/// `LivingEntity#dropAllDeathLoot` uses it to post the drops (`LivingDropsEvent`).
public interface DropCapture {
    @Nullable
    Collection<ItemEntity> epicfight$captureDrops();

    /// Sets the capture collection and returns the previous one
    @Nullable
    Collection<ItemEntity> epicfight$captureDrops(@Nullable Collection<ItemEntity> value);
}
