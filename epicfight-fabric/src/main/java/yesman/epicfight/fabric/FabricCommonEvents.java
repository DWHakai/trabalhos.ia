package yesman.epicfight.fabric;

import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.data.reloader.ItemCapabilityReloadListener;
import yesman.epicfight.api.data.reloader.MobPatchReloadListener;
import yesman.epicfight.api.data.reloader.SkillReloadListener;
import yesman.epicfight.api.event.impl.VanillaEntityEventHooks;
import yesman.epicfight.api.event.impl.VanillaPlayerEventHooks;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.EpicFightNetworkManager.PayloadBundleBuilder;
import yesman.epicfight.network.server.SPDatapackSync;
import yesman.epicfight.platform.event.LivingJumpEvent;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;
import yesman.epicfight.world.capabilities.item.ItemKeywordReloadListener;
import yesman.epicfight.world.capabilities.item.WeaponTypeReloadListener;
import yesman.epicfight.world.capabilities.skill.PlayerSkills;
import yesman.epicfight.world.gamerule.EpicFightGameRules;
import yesman.epicfight.world.gamerule.EpicFightGameRules.ConfigurableGameRule;

/// Port of the upstream `NeoForgeEntityEvent`, `NeoForgePlayerEvent` and `NeoForgeWorldEvent`
/// listeners that have a Fabric API callback at the same place as the NeoForge event. The rest
/// is fired by the mixins in `yesman.epicfight.fabric.mixin.event`:
///
/// - EntityConstructing, Size, mount, tick, drops: `MixinEntityEvents`, `MixinServerLevelEvents`
/// - join level: `MixinPersistentEntitySectionManager`, `MixinClientLevelEvents`
/// - death, knockback, shield block, damage pre/post, mob effects, item use start: `MixinLivingEntityEvents`
/// - `Player`/`ServerPlayer` overrides: `MixinPlayerEvents`, `MixinServerPlayerEvents`
/// - projectile impact, enderman teleport, arrow loose, player load: `MixinProjectileEvents`,
///   `MixinEnderManEvents`, `MixinBowItemEvents`, `MixinCrossbowItemEvents`, `MixinPlayerListEvents`
/// - item attribute modifiers: [ItemAttributeHooks]
public final class FabricCommonEvents {
    public static void register() {
        // LivingIncomingDamageEvent: fired by Fabric right before LivingEntity#hurt wakes the
        // entity up, the same place NeoForge fires it
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> !VanillaEntityEventHooks.onDamageIncomes(entity, source, amount));

        // LivingEquipmentChangeEvent
        ServerEntityEvents.EQUIPMENT_CHANGE.register((entity, slot, previous, next) -> VanillaEntityEventHooks.onEquipmentChanged(entity, previous, next, slot));

        // LivingJumpEvent
        LivingJumpEvent.BUS.addListener(event -> VanillaEntityEventHooks.onJump(event.getEntity()));

        // PlayerEvent.StartTracking / StopTracking
        EntityTrackingEvents.START_TRACKING.register(VanillaPlayerEventHooks::onStartTracking);
        EntityTrackingEvents.STOP_TRACKING.register(VanillaPlayerEventHooks::onStopTracking);

        // PlayerEvent.Clone: Fabric passes whether the player was alive, NeoForge whether it died
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> VanillaPlayerEventHooks.onCloned(oldPlayer, newPlayer, !alive));

        // PlayerInteractEvent.RightClickItem, server side (the client side is in FabricClientEvents)
        UseItemCallback.EVENT.register((player, level, hand) -> {
            ItemStack itemstack = player.getItemInHand(hand);

            if (!level.isClientSide() && VanillaPlayerEventHooks.onUseItemInServerSide(player)) {
                return InteractionResultHolder.fail(itemstack);
            }

            return InteractionResultHolder.pass(itemstack);
        });

        // OnDatapackSyncEvent: Fabric fires once per player, also on /reload
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register(FabricCommonEvents::onDatapackSync);
    }

    /// Upstream `NeoForgePlayerEvent#itemUseStartEvent` (`LivingEntityUseItemEvent.Start`).
    ///
    /// The upstream listener also resets the duration of items that block to
    /// `ItemStack#getUseDuration`, which is the duration the event starts with, so only the
    /// cancellation is kept.
    ///
    /// @return whether the item use is canceled
    public static boolean onItemUseStart(LivingEntity entity, ItemStack itemstack) {
        return EpicFightCapabilities.getPlayerPatchAsOptional(entity).map(playerpatch -> {
            if (!playerpatch.getEntityState().canUseItem()) {
                return true;
            }

            return itemstack == playerpatch.getOriginal().getOffhandItem() && !playerpatch.getHoldingItemCapability(InteractionHand.MAIN_HAND).getStyle(playerpatch).canUseOffhand();
        }).orElse(false);
    }

    /// Upstream `NeoForgeWorldEvent#onDatapackSync`. NeoForge posts the event with a player when
    /// one joins and without one after a reload; Fabric posts it for each player either way.
    private static void onDatapackSync(ServerPlayer player, boolean joined) {
        if (!joined) {
            sendLevelData(player);
            return;
        }

        PayloadBundleBuilder payloadBundleBuilder = PayloadBundleBuilder.create();

        EpicFightGameRules.GAME_RULES.values().stream().filter(ConfigurableGameRule::shouldSync).forEach(gamerule -> {
            payloadBundleBuilder.and(gamerule.getSyncPacket(player));
        });

        payloadBundleBuilder.send((first, others) -> EpicFightNetworkManager.sendToPlayer(first, player, others));

        if (!player.getServer().isSingleplayerOwner(player.getGameProfile())) {
            sendLevelData(player);
        } else {
            EpicFightCapabilities.getUnparameterizedEntityPatch(player, ServerPlayerPatch.class).ifPresent(serverplayerpatch -> {
                PlayerSkills skillCapability = serverplayerpatch.getPlayerSkills();

                skillCapability.listSkillContainers().forEach(skillContainer -> {
                    if (skillContainer.getSkill() != null) {
                        // Reload skill
                        skillContainer.setSkill(skillContainer.getSkill(), true);
                    }
                });
            });
        }
    }

    private static void sendLevelData(ServerPlayer player) {
        EpicFightCapabilities.getUnparameterizedEntityPatch(player, ServerPlayerPatch.class).ifPresent(serverplayerpatch -> {
            PlayerSkills skillCapability = serverplayerpatch.getPlayerSkills();

            skillCapability.listSkillContainers().forEach(skillContainer -> {
                if (skillContainer.getSkill() != null) {
                    // Reload skill
                    skillContainer.setSkill(skillContainer.getSkill(), true);
                }
            });

            SPDatapackSync skillParamsPacket = new SPDatapackSync(SPDatapackSync.PacketType.SKILL_PARAMS);
            SkillReloadListener.getSkillParams().forEach(skillParamsPacket::addTag);
            EpicFightNetworkManager.sendToPlayer(skillParamsPacket, player);
        });

        SPDatapackSync animationPacket = new SPDatapackSync(player.getServer().isResourcePackRequired() ? SPDatapackSync.PacketType.MANDATORY_RESOURCE_PACK_ANIMATION : SPDatapackSync.PacketType.RESOURCE_PACK_ANIMATION);
        SPDatapackSync armorPacket = new SPDatapackSync(SPDatapackSync.PacketType.ARMOR);
        SPDatapackSync weaponPacket = new SPDatapackSync(SPDatapackSync.PacketType.WEAPON);
        SPDatapackSync mobCapabilityPacket = new SPDatapackSync(SPDatapackSync.PacketType.MOB);
        SPDatapackSync itemPresetPacket = new SPDatapackSync(SPDatapackSync.PacketType.ITEM_PRESET);
        SPDatapackSync conditionalPacket = new SPDatapackSync(SPDatapackSync.PacketType.PROVIDER_CONDITIONAL);
        SPDatapackSync movesetPacket = new SPDatapackSync(SPDatapackSync.PacketType.MOVESET);
        SPDatapackSync weaponModifierPacket = new SPDatapackSync(SPDatapackSync.PacketType.MODIFIER);
        SPDatapackSync weaponTypePacket = new SPDatapackSync(SPDatapackSync.PacketType.WEAPON_TYPE);
        SPDatapackSync itemKeywordPacket = new SPDatapackSync(SPDatapackSync.PacketType.ITEM_KEYWORD);

        AnimationManager.getInstance().getResourcepackAnimationStream().forEach(animationPacket::addTag);
        ItemCapabilityReloadListener.getArmorDataStream().forEach(armorPacket::addTag);
        ItemCapabilityReloadListener.getWeaponDataStream().forEach(weaponPacket::addTag);
        MobPatchReloadListener.getDataStream().forEach(mobCapabilityPacket::addTag);
        WeaponTypeReloadListener.getWeaponTypeDataStream().forEach(weaponTypePacket::addTag);
        ItemKeywordReloadListener.getCompounds().forEach(itemKeywordPacket::addTag);

        PayloadBundleBuilder
            .beginWith(animationPacket)
            .and(itemPresetPacket)
            .and(weaponModifierPacket)
            .and(conditionalPacket)
            .and(movesetPacket)
            .and(weaponTypePacket)
            .and(armorPacket)
            .and(weaponPacket)
            .and(mobCapabilityPacket)
            .and(itemKeywordPacket)
            .send((first, others) -> EpicFightNetworkManager.sendToPlayer(first, player, others));
    }

    private FabricCommonEvents() {}
}
