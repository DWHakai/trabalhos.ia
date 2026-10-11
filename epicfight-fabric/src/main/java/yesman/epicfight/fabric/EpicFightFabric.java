package yesman.epicfight.fabric;

import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeConfigRegistry;
import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeModConfigEvents;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.config.ModConfig;
import yesman.epicfight.EpicFight;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.animation.AnimationManager.AnimationRegistryEvent;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.client.input.action.EpicFightInputAction;
import yesman.epicfight.api.client.input.action.InputAction;
import yesman.epicfight.api.client.input.action.MinecraftInputAction;
import yesman.epicfight.api.data.reloader.ItemCapabilityReloadListener;
import yesman.epicfight.api.data.reloader.MobPatchReloadListener;
import yesman.epicfight.api.data.reloader.SkillReloadListener;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.api.ex_cap.listeners.ConditionalReloadListener;
import yesman.epicfight.api.ex_cap.listeners.ItemPresetReloadListener;
import yesman.epicfight.api.ex_cap.listeners.MovesetReloadListener;
import yesman.epicfight.api.ex_cap.listeners.WeaponModifierReloadListener;
import yesman.epicfight.client.gui.widgets.AnchoredButton;
import yesman.epicfight.client.gui.widgets.ColorDeterminator;
import yesman.epicfight.client.gui.widgets.common.WidgetTheme;
import yesman.epicfight.client.online.cosmetics.Emote;
import yesman.epicfight.compat.ICompatModule;
import yesman.epicfight.compat.MinecraftMod;
import yesman.epicfight.config.ClientConfig;
import yesman.epicfight.config.CommonConfig;
import yesman.epicfight.config.ServerConfig;
import yesman.epicfight.data.loot.EpicFightLootTables;
import yesman.epicfight.epicskins.animation.EpicSkinsAnimations;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.Armatures;
import yesman.epicfight.gameasset.ColliderPreset;
import yesman.epicfight.main.EpicFightExtensibleEnums;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.main.EpicFightSharedConstants;
import yesman.epicfight.network.EntityPairingPacketType;
import yesman.epicfight.network.EntityPairingPacketTypes;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.platform.fabric.FabricModPlatform;
import yesman.epicfight.platform.registry.RegistryBuilder;
import yesman.epicfight.registry.EpicFightRegistries;
import yesman.epicfight.registry.entries.*;
import yesman.epicfight.server.commands.AnimatorCommand;
import yesman.epicfight.server.commands.PlayerModeCommand;
import yesman.epicfight.server.commands.PlayerSkillCommand;
import yesman.epicfight.server.commands.PlayerStaminaCommand;
import yesman.epicfight.skill.SkillCategories;
import yesman.epicfight.skill.SkillCategory;
import yesman.epicfight.skill.SkillSlot;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.Faction;
import yesman.epicfight.world.capabilities.entitypatch.Factions;
import yesman.epicfight.world.capabilities.item.CapabilityItem.Styles;
import yesman.epicfight.world.capabilities.item.CapabilityItem.WeaponCategories;
import yesman.epicfight.world.capabilities.item.Style;
import yesman.epicfight.world.capabilities.item.WeaponCategory;
import yesman.epicfight.world.capabilities.item.WeaponTypeReloadListener;
import yesman.epicfight.world.capabilities.item.ItemKeywordReloadListener;
import yesman.epicfight.world.gamerule.EpicFightGameRules;
import yesman.epicfight.world.item.SkillBookItem;

import java.util.Comparator;

/// Fabric entrypoint. Mirrors the constructor and lifecycle listeners of the upstream NeoForge
/// entrypoint (`yesman.epicfight.main.EpicFightMod` on the upstream 1.21.1 branch), in the same order.
///
/// On Fabric everything below runs on the main thread during mod initialization, so the
/// NeoForge `enqueueWork` wrappers are not needed.
public final class EpicFightFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        EpicFight.initialize(new FabricModPlatform());
        // TODO(port, phase 4): TooltipProviderRegistry.register(new EpicFightTooltipProvider(), 2) (Simply Tooltips compat)

        // Configs (Forge Config API Port keeps the NeoForge ModConfigSpec API)
        if (EpicFightSharedConstants.isPhysicalClient()) {
            NeoForgeConfigRegistry.INSTANCE.register(EpicFight.MODID, ModConfig.Type.CLIENT, ClientConfig.SPEC);
        } else {
            NeoForgeConfigRegistry.INSTANCE.register(EpicFight.MODID, ModConfig.Type.SERVER, ServerConfig.SPEC);
        }
        NeoForgeConfigRegistry.INSTANCE.register(EpicFight.MODID, ModConfig.Type.COMMON, CommonConfig.SPEC);
        // ModConfigEvent.Loading listeners (upstream: @EventBusSubscriber in the config classes)
        NeoForgeModConfigEvents.loading(EpicFight.MODID).register(config -> {
            if (EpicFightSharedConstants.isPhysicalClient()) {
                ClientConfig.epicfight$modConfigLoading(config);
            } else {
                ServerConfig.onLoad(config);
            }

            CommonConfig.onLoad(config);
        });

        // Extensible enums
        LivingMotion.ENUM_MANAGER.registerEnumCls(EpicFight.MODID, LivingMotions.class);
        SkillCategory.ENUM_MANAGER.registerEnumCls(EpicFight.MODID, SkillCategories.class);
        SkillSlot.ENUM_MANAGER.registerEnumCls(EpicFight.MODID, SkillSlots.class);
        Style.ENUM_MANAGER.registerEnumCls(EpicFight.MODID, Styles.class);
        WeaponCategory.ENUM_MANAGER.registerEnumCls(EpicFight.MODID, WeaponCategories.class);
        Faction.ENUM_MANAGER.registerEnumCls(EpicFight.MODID, Factions.class);
        EntityPairingPacketType.ENUM_MANAGER.registerEnumCls(EpicFight.MODID, EntityPairingPacketTypes.class);

        if (EpicFightSharedConstants.isPhysicalClient()) {
            InputAction.ENUM_MANAGER.registerEnumCls(EpicFight.MODID, EpicFightInputAction.class);
            InputAction.ENUM_MANAGER.registerEnumCls("minecraft", MinecraftInputAction.class);
            WidgetTheme.ENUM_MANAGER.registerEnumCls(EpicFightMod.prefix("color_determinator_theme"), ColorDeterminator.Theme.class);
            WidgetTheme.ENUM_MANAGER.registerEnumCls(EpicFightMod.prefix("anchored_button_built_in_theme"), AnchoredButton.BuiltInTheme.class);
        }

        // Registries: touching EpicFightRegistries creates the custom registries, then every
        // DeferredRegister adds its entries (NeoForge: DeferredRegister#register(IEventBus)).
        EpicFightRegistries.DEFERRED_REGISTRIES.forEach(deferredRegister -> deferredRegister.registerAll());
        RegistryBuilder.bakeAll();
        // EntityAttributeCreationEvent / EntityAttributeModificationEvent
        EpicFightAttributes.EventBus.registerAttributes();
        // RegisterPayloadHandlersEvent
        EpicFightNetworkManager.register();
        DynamicRegistries.registerSynced(EpicFightRegistries.Keys.EMOTE, Emote.CODEC, Emote.CODEC);

        // RegisterCapabilitiesEvent: EpicFightCapabilities asks the item capability provider directly
        loadModCompatibilityModules();

        // FMLConstructModEvent
        LivingMotion.ENUM_MANAGER.loadEnum();
        SkillCategory.ENUM_MANAGER.loadEnum();
        SkillSlot.ENUM_MANAGER.loadEnum();
        Style.ENUM_MANAGER.loadEnum();
        WeaponCategory.ENUM_MANAGER.loadEnum();
        Faction.ENUM_MANAGER.loadEnum();
        EntityPairingPacketType.ENUM_MANAGER.loadEnum();
        WidgetTheme.ENUM_MANAGER.loadEnum();

        if (EpicFightSharedConstants.isPhysicalClient()) {
            InputAction.ENUM_MANAGER.loadEnum();
        }

        AnimationManager.addNoWarningModId(EpicFight.EPICSKINS_MODID);
        // AnimationRegistryEvent: a mod bus event upstream (ModLoader.postEvent), here a Fabric
        // event other mods can listen to from their own initializers
        AnimationRegistryEvent.EVENT.register(Animations::registerAnimations);
        AnimationRegistryEvent.EVENT.register(EpicSkinsAnimations::registerAnimations);
        AnimationRegistryEvent animationRegistryEvent = new AnimationRegistryEvent();
        AnimationRegistryEvent.EVENT.invoker().accept(animationRegistryEvent);
        animationRegistryEvent.getBuilders().stream().sorted(Comparator.comparing(AnimationManager.AnimationBuilder::namespace)).forEach(builder -> builder.task().accept(builder));

        // FMLCommonSetupEvent
        Armatures.registerEntityTypes();
        EpicFightCommandArgumentTypes.registerArgumentTypes();
        EpicFightPotions.addRecipes();
        EpicFightCapabilities.ITEM_CAPABILITY_PROVIDER.registerWeaponTypesByClass();
        EpicFightCapabilities.ENTITY_PATCH_PROVIDER.registerVanillaEntityPatches();
        EpicFightGameRules.registerGameRules();
        WeaponTypeReloadListener.registerDefaultWeaponTypes();
        EpicFightMobEffects.addOffhandModifier();
        EpicFightExtensibleEnums.initExtensibleEnums();
        EpicFightEventHooks.Registry.SKILLBOOK_LOOT_TABLE.registerEvent(EpicFightLootTables::createSkillLootTable);
        // LootTableLoadEvent
        EpicFightLootTables.registerEvents();

        // Game events (upstream NeoForgeEntityEvent, NeoForgePlayerEvent, NeoForgeWorldEvent)
        FabricCommonEvents.register();

        // RegisterCommandsEvent
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            PlayerModeCommand.register(dispatcher);
            PlayerSkillCommand.register(dispatcher);
            PlayerStaminaCommand.register(dispatcher);
            AnimatorCommand.register(dispatcher);
        });

        // AddReloadListenerEvent
        ResourceManagerHelper data = ResourceManagerHelper.get(PackType.SERVER_DATA);

        if (!EpicFightSharedConstants.isPhysicalClient()) {
            // Upstream: ServerForgeEvents#addReloadListnerEvent (HIGHEST priority, dedicated server only)
            data.registerReloadListener(FabricReloadListener.of("server_animations", AnimationManager.getInstance()));
        }

        data.registerReloadListener(FabricReloadListener.of("collider_presets", new ColliderPreset()));
        data.registerReloadListener(FabricReloadListener.of("skills", new SkillReloadListener()));
        data.registerReloadListener(FabricReloadListener.of("item_presets", new ItemPresetReloadListener()));
        data.registerReloadListener(FabricReloadListener.of("weapon_modifiers", new WeaponModifierReloadListener()));
        data.registerReloadListener(FabricReloadListener.of("conditionals", new ConditionalReloadListener()));
        data.registerReloadListener(FabricReloadListener.of("movesets", new MovesetReloadListener()));
        data.registerReloadListener(FabricReloadListener.of("weapon_types", new WeaponTypeReloadListener()));
        data.registerReloadListener(FabricReloadListener.of("item_keywords", new ItemKeywordReloadListener()));
        data.registerReloadListener(FabricReloadListener.of("item_capabilities", new ItemCapabilityReloadListener()));
        data.registerReloadListener(FabricReloadListener.of("mob_patches", new MobPatchReloadListener()));

        // AddPackFindersEvent: the legacy resource pack is shipped under resourcepacks/epicfight_legacy
        FabricLoader.getInstance().getModContainer(EpicFight.MODID).ifPresent(container ->
            ResourceManagerHelper.registerBuiltinResourcePack(
                EpicFight.identifier("epicfight_legacy"),
                container,
                Component.translatable("pack.epicfight_legacy.title"),
                ResourcePackActivationType.NORMAL
            )
        );

        // BuildCreativeModeTabContentsEvent. Upstream lets other mods choose the tab of their skill
        // books through EpicFightExtensions; there are no Epic Fight add-ons on Fabric, so skills
        // without an explicit tab go to the Epic Fight items tab.
        ItemGroupEvents.MODIFY_ENTRIES_ALL.register((tab, entries) -> {
            EpicFightRegistries.SKILL.holders()
                .filter(skill -> skill.value().getCategory().learnable())
                .filter(skill -> skill.value().getCreativeTab() == tab
                    || (skill.value().getCreativeTab() == null && tab == EpicFightCreativeTabs.ITEMS.get()))
                .forEach(holder -> {
                    ItemStack stack = new ItemStack(EpicFightItems.SKILLBOOK.get());
                    SkillBookItem.setContainingSkill(holder, stack);
                    entries.accept(stack);
                });
        });
    }

    /// Upstream `EpicFightMod#loadModCompatibilityModules`. The MCreator bedrock animation module is
    /// not ported. Mods with their own entrypoint (Controlify, Shoulder Surfing) are not listed.
    private static void loadModCompatibilityModules() {
        final boolean isClientSide = EpicFightSharedConstants.isPhysicalClient();

        for (MinecraftMod mod : MinecraftMod.values()) {
            if (FabricLoader.getInstance().isModLoaded(mod.getModId()) && (isClientSide || !mod.isClientOnly())) {
                ICompatModule.loadCompatModule(mod.getCompatibilityModule());
            }
        }
    }
}
