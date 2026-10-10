package yesman.epicfight.platform;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/// The NeoForge hooks (`net.neoforged.neoforge.event.EventHooks` and block extensions) that Epic
/// Fight calls, with the result NeoForge gives when no other mod listens to the event.
public final class EventHooks {
    /// NeoForge `EventHooks#canEntityGrief`: the mob griefing game rule
    public static boolean canEntityGrief(Level level, Entity entity) {
        return level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
    }

    /// NeoForge `EventHooks#onEntityDestroyBlock` (LivingDestroyBlockEvent, not canceled)
    public static boolean onEntityDestroyBlock(LivingEntity entity, BlockPos pos, BlockState state) {
        return true;
    }

    /// NeoForge `EventHooks#onUseItemStop` (LivingEntityUseItemEvent.Stop, not canceled)
    public static boolean onUseItemStop(LivingEntity entity, ItemStack item, int duration) {
        return false;
    }

    /// NeoForge `IBlockExtension#canEntityDestroy`
    public static boolean canEntityDestroy(BlockState state, BlockGetter level, BlockPos pos, Entity entity) {
        if (entity instanceof EnderDragon) {
            return !state.getBlock().defaultBlockState().is(BlockTags.DRAGON_IMMUNE);
        } else if (entity instanceof WitherBoss || entity instanceof WitherSkull) {
            return state.isAir() || WitherBoss.canDestroy(state);
        }

        return true;
    }

    private EventHooks() {}
}
