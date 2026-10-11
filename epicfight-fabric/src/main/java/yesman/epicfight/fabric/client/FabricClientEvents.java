package yesman.epicfight.fabric.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResultHolder;
import yesman.epicfight.api.client.event.impl.VanillaGUIEventHooks;
import yesman.epicfight.api.client.event.impl.VanillaGeneralClientEventHooks;
import yesman.epicfight.api.client.model.SoftBodyTranslatable;
import yesman.epicfight.api.client.physics.cloth.ClothSimulatable;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.client.events.engine.RenderEngine;
import yesman.epicfight.client.particle.*;
import yesman.epicfight.client.renderer.blockentity.FractureBlockRenderer;
import yesman.epicfight.client.renderer.entity.DroppedNetherStarRenderer;
import yesman.epicfight.client.renderer.entity.WitherGhostRenderer;
import yesman.epicfight.client.renderer.entity.WitherSkeletonMinionRenderer;
import yesman.epicfight.client.renderer.patched.item.RenderItemBase;
import yesman.epicfight.client.renderer.patched.layer.WearableItemLayer;
import yesman.epicfight.fabric.client.model.SeparateTransformsModel;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.platform.client.event.EntityRenderersEvent;
import yesman.epicfight.platform.client.event.GuiLayers;
import yesman.epicfight.registry.entries.EpicFightBlockEntities;
import yesman.epicfight.registry.entries.EpicFightEntityTypes;
import yesman.epicfight.registry.entries.EpicFightParticles;
import yesman.epicfight.skill.SkillCategory;

import java.util.ArrayList;
import java.util.List;

/// Fabric version of the upstream NeoForge client listeners `ClientModBusEvent` (mod bus) and
/// `ClientEvents` (game bus), in `client/platform/neoforge/event`, which stay out of compilation.
public final class FabricClientEvents {
	public static void register() {
		registerParticleProviders();
		registerRenderers();

		// RenderLevelStageEvent.RegisterStageEvent: upstream uses it because it fires right after
		// the game renderer is created
		ClientLifecycleEvents.CLIENT_STARTED.register(RenderItemBase::initItemRenderers);

		EntityRenderersEvent.AddLayers.BUS.addListener(FabricClientEvents::addLayers);
		registerGuiLayers();
		ModelLoadingPlugin.register(FabricClientEvents::skillBookModels);
		// The weapon item models use NeoForge's separate_transforms loader
		SeparateTransformsModel.register();

		// ClientEvents
		ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			ScreenMouseEvents.allowMouseClick(screen).register((s, mouseX, mouseY, button) -> !VanillaGUIEventHooks.onMouseButtonPressedInScreen(s));
			ScreenMouseEvents.allowMouseRelease(screen).register((s, mouseX, mouseY, button) -> !VanillaGUIEventHooks.onMouseButtonReleasedInScreen(s));
			ScreenKeyboardEvents.allowKeyPress(screen).register((s, key, scancode, modifiers) -> !VanillaGUIEventHooks.onKeyboardPressedInScreen(s, key));
		});

		// PlayerInteractEvent.RightClickItem, client side only (Fabric fires it on both sides). A
		// canceled NeoForge event makes the client skip this hand without sending the use packet.
		UseItemCallback.EVENT.register((player, level, hand) -> {
			if (level.isClientSide() && VanillaGeneralClientEventHooks.onUseItemInClientSide(player, player.getItemInHand(hand), hand)) {
				return InteractionResultHolder.fail(player.getItemInHand(hand));
			}

			return InteractionResultHolder.pass(player.getItemInHand(hand));
		});

		// ClientPlayerNetworkEvent.LoggingIn/Clone/LoggingOut: MixinClientPacketListenerEvents and MixinMinecraftEvents
	}

	private static void registerParticleProviders() {
		ParticleFactoryRegistry registry = ParticleFactoryRegistry.getInstance();
		registry.register(EpicFightParticles.ENDERMAN_DEATH_EMIT.get(), EnderParticle.EndermanDeathEmitProvider::new);
		registry.register(EpicFightParticles.HIT_BLUNT.get(), HitBluntParticle.Provider::new);
		registry.register(EpicFightParticles.HIT_BLADE.get(), new HitCutParticle.Provider());
		registry.register(EpicFightParticles.CUT.get(), CutParticle.Provider::new);
		registry.register(EpicFightParticles.NORMAL_DUST.get(), DustParticle.NormalDustProvider::new);
		registry.register(EpicFightParticles.DUST_EXPANSIVE.get(), DustParticle.ExpansiveDustProvider::new);
		registry.register(EpicFightParticles.DUST_CONTRACTIVE.get(), DustParticle.ContractiveDustProvider::new);
		registry.register(EpicFightParticles.EVISCERATE.get(), new EviscerateParticle.Provider());
		registry.register(EpicFightParticles.BLOOD.get(), BloodParticle.Provider::new);
		registry.register(EpicFightParticles.BLADE_RUSH_SKILL.get(), BladeRushParticle.Provider::new);
		registry.register(EpicFightParticles.GROUND_SLAM.get(), new GroundSlamParticle.Provider());
		registry.register(EpicFightParticles.GROUND_FRACTURE.get(), new GroundSlamParticle.BlockParticleProvider());
		registry.register(EpicFightParticles.BREATH_FLAME.get(), EnderParticle.BreathFlameProvider::new);
		registry.register(EpicFightParticles.FORCE_FIELD.get(), new ForceFieldParticle.Provider());
		registry.register(EpicFightParticles.FORCE_FIELD_END.get(), new ForceFieldEndParticle.Provider());
		registry.register(EpicFightParticles.ADRENALINE_PLAYER_BEATING.get(), new EntityAfterimageParticle.AdrenalineParticleProvider());
		registry.register(EpicFightParticles.WHITE_AFTERIMAGE.get(), new EntityAfterimageParticle.WhiteAfterimageProvider());
		registry.register(EpicFightParticles.LASER.get(), new LaserParticle.Provider());
		registry.register(EpicFightParticles.NEUTRALIZE.get(), new DustParticle.ExpansiveMetaParticle.Provider());
		registry.register(EpicFightParticles.BOSS_CASTING.get(), new DustParticle.ContractiveMetaParticle.Provider());
		registry.register(EpicFightParticles.TSUNAMI_SPLASH.get(), TsunamiSplashParticle.Provider::new);
		registry.register(EpicFightParticles.SWING_TRAIL.get(), new AnimationTrailParticle.Provider());
		registry.register(EpicFightParticles.PROJECTILE_TRAIL.get(), new ProjectileTrailParticle.Provider());
		registry.register(EpicFightParticles.FEATHER.get(), FeatherParticle.Provider::new);
		registry.register(EpicFightParticles.AIR_BURST.get(), new AirBurstParticle.Provider());
		registry.register(EpicFightParticles.ASH_DIRECTIONAL.get(), AshDirectionalParticle.Provider::new);
		registry.register(EpicFightParticles.CATHARSIS.get(), CatharsisParticle.Provider::new);
	}

	private static void registerRenderers() {
		EntityRendererRegistry.register(EpicFightEntityTypes.AREA_EFFECT_BREATH.get(), NoopRenderer::new);
		EntityRendererRegistry.register(EpicFightEntityTypes.DROPPED_NETHER_STAR.get(), DroppedNetherStarRenderer::new);
		EntityRendererRegistry.register(EpicFightEntityTypes.DEATH_HARVEST_ORB.get(), NoopRenderer::new);
		EntityRendererRegistry.register(EpicFightEntityTypes.DODGE_LOCATION_INDICATOR.get(), NoopRenderer::new);
		EntityRendererRegistry.register(EpicFightEntityTypes.WITHER_GHOST_CLONE.get(), WitherGhostRenderer::new);
		EntityRendererRegistry.register(EpicFightEntityTypes.WITHER_SKELETON_MINION.get(), WitherSkeletonMinionRenderer::new);
		BlockEntityRenderers.register(EpicFightBlockEntities.FRACTURE.get(), FractureBlockRenderer::new);
		// Upstream loads it in RegisterRenderers, which fires after the skin manager exists; the
		// Fabric client entrypoint runs before that
		ClientLifecycleEvents.CLIENT_STARTED.register(client -> ClientEngine.getInstance().getAuthHelper().loadPlayerSkin());
	}

	private static void addLayers(EntityRenderersEvent.AddLayers event) {
		WearableItemLayer.clearModels();
		SoftBodyTranslatable.TRACKING_SIMULATION_SUBJECTS.removeIf(ClothSimulatable::invalid);

		for (ClothSimulatable simOwner : SoftBodyTranslatable.TRACKING_SIMULATION_SUBJECTS) {
			simOwner.getClothSimulator().getAllRunningObjects().forEach((entry) -> {
				simOwner.getClothSimulator().restart(entry.getKey());
			});
		}
	}

	private static void registerGuiLayers() {
		GuiLayers.registerAboveAll(EpicFightMod.identifier("stamina_bar"), (guiGraphics, deltaTracker) -> RenderEngine.getInstance().battleModeHUD.renderStaminaBar(guiGraphics, deltaTracker));
		GuiLayers.registerAboveAll(EpicFightMod.identifier("skills"), (guiGraphics, deltaTracker) -> RenderEngine.getInstance().battleModeHUD.renderNormalSkills(guiGraphics, deltaTracker));
		GuiLayers.registerAboveAll(EpicFightMod.identifier("weapon_innate"), (guiGraphics, deltaTracker) -> RenderEngine.getInstance().battleModeHUD.renderWeaponInnateSkill(guiGraphics, deltaTracker));
		GuiLayers.registerAboveAll(EpicFightMod.identifier("charging_bar"), (guiGraphics, deltaTracker) -> RenderEngine.getInstance().battleModeHUD.renderChargingBar(guiGraphics, deltaTracker));
	}

	private static ResourceLocation wrapItemModelPath(ResourceLocation rl) {
		return ResourceLocation.fromNamespaceAndPath(rl.getNamespace(), "item/" + rl.getPath());
	}

	private static List<SkillCategory> categoriesWithOwnBookIcon() {
		return SkillCategory.ENUM_MANAGER.universalValues().stream().filter(skillCategory -> !skillCategory.bookIcon().equals(SkillCategory.DEFAULT_BOOK_ICON)).toList();
	}

	/// ModelEvent.RegisterAdditional + ModelEvent.ModifyBakingResult: the skill book model gets an
	/// override per skill category with its own book icon (item property `epicfight:skill`).
	private static void skillBookModels(ModelLoadingPlugin.Context context) {
		context.addModels(categoriesWithOwnBookIcon().stream().map(skillCategory -> wrapItemModelPath(skillCategory.bookIcon())).toList());

		ModelResourceLocation skillbookLocation = new ModelResourceLocation(SkillCategory.DEFAULT_BOOK_ICON, ModelResourceLocation.INVENTORY_VARIANT);

		context.modifyModelAfterBake().register((model, bakeContext) -> {
			if (model == null || !skillbookLocation.equals(bakeContext.topLevelId())) {
				return model;
			}

			List<ItemOverrides.BakedOverride> skillCategoryOverrides = new ArrayList<> ();

			categoriesWithOwnBookIcon().stream().sorted((c1, c2) -> {
				return Integer.compare(c2.universalOrdinal(), c1.universalOrdinal());
			}).forEach(skillCategory -> {
				ItemOverrides.PropertyMatcher[] propertyMatchers = new ItemOverrides.PropertyMatcher[1];
				propertyMatchers[0] = new ItemOverrides.PropertyMatcher(0, skillCategory.universalOrdinal());
				BakedModel bakedModel = bakeContext.baker().bake(wrapItemModelPath(skillCategory.bookIcon()), BlockModelRotation.X0_Y0);
				skillCategoryOverrides.add(new ItemOverrides.BakedOverride(propertyMatchers, bakedModel));
			});

			ItemOverrides overrides = model.getOverrides();
			overrides.overrides = skillCategoryOverrides.toArray(ItemOverrides.BakedOverride[]::new);
			overrides.properties = new ResourceLocation[] {EpicFightMod.identifier("skill")};
			return model;
		});
	}

	private FabricClientEvents() {}
}
