package yesman.epicfight.fabric.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.server.packs.PackType;
import yesman.epicfight.EpicFightClient;
import yesman.epicfight.api.animation.AnimationManager;
import yesman.epicfight.api.client.animation.property.JointMaskReloadListener;
import yesman.epicfight.api.client.model.ItemSkinsReloadListener;
import yesman.epicfight.api.client.model.Meshes;
import yesman.epicfight.client.gui.screen.SkillBookScreen;
import yesman.epicfight.client.gui.screen.config.ItemsPreferenceScreen;
import yesman.epicfight.client.input.EpicFightKeyMappings;
import yesman.epicfight.client.renderer.patched.item.EpicFightItemProperties;
import yesman.epicfight.client.renderer.shader.compute.loader.ComputeShaderProvider;
import yesman.epicfight.config.ClientConfig;
import yesman.epicfight.fabric.FabricReloadListener;
import yesman.epicfight.platform.fabric.client.FabricClientModPlatform;
import yesman.epicfight.world.capabilities.provider.CommonEntityPatchProvider;

/// Fabric client entrypoint. Mirrors the client part of the upstream NeoForge entrypoint
/// (`EpicFightMod` constructor and `EpicFightMod.ClientModEvents`).
public final class EpicFightFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EpicFightClient.initialize(new FabricClientModPlatform());
        EpicFightKeyMappings.registerKeys();

        // TODO(port, phase 2): IEventBasedEngine.init(NeoForge.EVENT_BUS, modEventBus) wires the
        //  client engines (input, render, camera, GUI) to NeoForge events; they must be moved to
        //  Fabric callbacks and mixins. See PORTING.md.
        // TODO(port, phase 2): config screen (IConfigScreenFactory -> EpicFightSettingScreen) through Mod Menu
        // TODO(port, phase 2): ComputeShaderProvider#epicfight$registerComputeShaders (RegisterShadersEvent)

        // FMLClientSetupEvent. Item properties are read when models are baked, so they have to be
        // registered before the first resource reload.
        CommonEntityPatchProvider.ClientModule.registerClientPlayerPatches();
        SkillBookScreen.registerIconItems();
        EpicFightItemProperties.registerItemProperties();

        // Needs an OpenGL context and the loaded client config
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
