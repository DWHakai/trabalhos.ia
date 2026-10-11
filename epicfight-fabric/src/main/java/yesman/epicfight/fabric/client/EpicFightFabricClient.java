package yesman.epicfight.fabric.client;

import net.fabricmc.api.ClientModInitializer;
import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.client.ConfigScreenFactoryRegistry;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.server.packs.PackType;
import yesman.epicfight.EpicFight;
import yesman.epicfight.EpicFightClient;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.client.animation.property.JointMaskReloadListener;
import yesman.epicfight.api.client.model.ItemSkinsReloadListener;
import yesman.epicfight.api.client.model.Meshes;
import yesman.epicfight.client.gui.screen.SkillBookScreen;
import yesman.epicfight.client.events.engine.IEventBasedEngine;
import yesman.epicfight.client.gui.screen.config.EpicFightSettingScreen;
import yesman.epicfight.client.gui.screen.config.ItemsPreferenceScreen;
import yesman.epicfight.client.input.EpicFightKeyMappings;
import yesman.epicfight.client.renderer.EpicFightShaders;
import yesman.epicfight.client.renderer.patched.item.EpicFightItemProperties;
import yesman.epicfight.client.renderer.shader.compute.loader.ComputeShaderProvider;
import yesman.epicfight.compat.ICompatModule;
import yesman.epicfight.config.ClientConfig;
import yesman.epicfight.fabric.FabricReloadListener;
import yesman.epicfight.platform.client.event.ClientTickEvent;
import yesman.epicfight.platform.client.event.LevelTickEvent;
import yesman.epicfight.platform.client.event.RegisterShadersEvent;
import yesman.epicfight.platform.client.event.RenderHighlightEvent;
import yesman.epicfight.platform.fabric.client.FabricClientModPlatform;
import yesman.epicfight.world.capabilities.provider.CommonEntityPatchProvider;

/// Fabric client entrypoint. Mirrors the client part of the upstream NeoForge entrypoint
/// (`EpicFightMod` constructor and `EpicFightMod.ClientModEvents`).
public final class EpicFightFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EpicFightClient.initialize(new FabricClientModPlatform());
        EpicFightKeyMappings.registerKeys();

        // Upstream: registerExtensionPoint(IConfigScreenFactory.class, EpicFightSettingScreen::new)
        ConfigScreenFactoryRegistry.INSTANCE.register(EpicFight.MODID, (modId, parent) -> new EpicFightSettingScreen(null, parent));

        // Upstream: IEventBasedEngine.init(NeoForge.EVENT_BUS, modEventBus). The NeoForge events
        // the engines listen to are fired by the platform event mixins and by the callbacks below.
        IEventBasedEngine.init();
        ClientTickEvents.START_CLIENT_TICK.register(client -> ClientTickEvent.Pre.BUS.post(ClientTickEvent.Pre.INSTANCE));
        ClientTickEvents.END_CLIENT_TICK.register(client -> ClientTickEvent.Post.BUS.post(ClientTickEvent.Post.INSTANCE));
        ClientTickEvents.END_WORLD_TICK.register(level -> LevelTickEvent.Post.BUS.post(new LevelTickEvent.Post(level)));
        WorldRenderEvents.BEFORE_BLOCK_OUTLINE.register((context, hitResult) -> !RenderHighlightEvent.Block.BUS.post(new RenderHighlightEvent.Block()).isCanceled());

        // RegisterShadersEvent listeners (upstream: @EventBusSubscriber EpicFightShaders and
        // modEventBus.addListener(ComputeShaderProvider::epicfight$registerComputeShaders))
        RegisterShadersEvent.addListener(EpicFightShaders::registerShadersEvent);
        RegisterShadersEvent.addListener(ComputeShaderProvider::epicfight$registerComputeShaders);

        // Upstream: ClientModBusEvent and ClientEvents
        FabricClientEvents.register();
        EpicFightClientNetworking.register();

        // FMLClientSetupEvent. Item properties are read when models are baked, so they have to be
        // registered before the first resource reload.
        CommonEntityPatchProvider.ClientModule.registerClientPlayerPatches();
        SkillBookScreen.registerIconItems();
        EpicFightItemProperties.registerItemProperties();
        ICompatModule.initClientModules();

        // Needs an OpenGL context and the loaded client config. Runs before the first resource
        // reload applies, so before the compute shaders are loaded.
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            ComputeShaderProvider.checkIfSupports();

            if (ClientConfig.combatCategorizedItems.isEmpty() && ClientConfig.miningCategorizedItems.isEmpty()) {
                ItemsPreferenceScreen.resetItems();
            }
        });

        // RegisterClientReloadListenersEvent
        ResourceManagerHelper assets = ResourceManagerHelper.get(PackType.CLIENT_RESOURCES);
        assets.registerReloadListener(FabricReloadListener.of("joint_masks", new JointMaskReloadListener()));
        assets.registerReloadListener(FabricReloadListener.of("meshes", Meshes.INSTANCE));
        assets.registerReloadListener(FabricReloadListener.of("animations", AnimationManager.getInstance()));
        assets.registerReloadListener(FabricReloadListener.of("item_skins", ItemSkinsReloadListener.INSTANCE));
    }
}
