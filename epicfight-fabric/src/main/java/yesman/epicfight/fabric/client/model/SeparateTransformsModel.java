package yesman.epicfight.fabric.client.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.fabric.api.client.model.loading.v1.PreparableModelLoadingPlugin;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.block.model.ItemModelGenerator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import yesman.epicfight.main.EpicFightMod;

import java.io.Reader;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;

/// Fabric version of NeoForge's `neoforge:separate_transforms` model loader, used by the weapon
/// item models of Epic Fight: a `base` model (in hand) and one model per display context in
/// `perspectives` (the flat icon in the GUI, on the ground and in item frames).
///
/// The models are read when the resources load and handed to the model bakery by a model
/// resolver. `MixinItemRendererSeparateTransforms` swaps the baked model for the display context it is drawn in.
public final class SeparateTransformsModel implements UnbakedModel {
    private static final String LOADER = "neoforge:separate_transforms";
    private static final ItemModelGenerator ITEM_MODEL_GENERATOR = new ItemModelGenerator();

    private final BlockModel base;
    private final Map<ItemDisplayContext, BlockModel> perspectives;
    private final @Nullable Boolean usesBlockLight;

    private SeparateTransformsModel(ResourceLocation id, JsonObject json) {
        this.base = blockModel(id, "base", json.getAsJsonObject("base"));
        this.perspectives = new EnumMap<>(ItemDisplayContext.class);

        if (json.has("perspectives")) {
            for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject("perspectives").entrySet()) {
                for (ItemDisplayContext displayContext : ItemDisplayContext.values()) {
                    if (displayContext.getSerializedName().equals(entry.getKey())) {
                        this.perspectives.put(displayContext, blockModel(id, entry.getKey(), entry.getValue().getAsJsonObject()));
                    }
                }
            }
        }

        // "front" lights the item flat in the GUI, "side" like a block
        this.usesBlockLight = json.has("gui_light") ? BlockModel.GuiLight.getByName(json.get("gui_light").getAsString()).lightLikeBlock() : null;
    }

    private static BlockModel blockModel(ResourceLocation id, String part, JsonObject json) {
        BlockModel blockModel = BlockModel.fromString(json.toString());
        blockModel.name = id + "#" + part;

        return blockModel;
    }

    /// Registers the model resolver. Call from the client initializer.
    public static void register() {
        PreparableModelLoadingPlugin.register(SeparateTransformsModel::loadModels, (models, context) -> {
            context.resolveModel().register(resolverContext -> {
                JsonObject json = models.get(resolverContext.id());
                return json == null ? null : new SeparateTransformsModel(resolverContext.id(), json);
            });
        });
    }

    /// Reads every model json that uses the NeoForge loader, keyed by model id
    /// (`epicfight:models/item/iron_longsword.json` -> `epicfight:item/iron_longsword`).
    private static CompletableFuture<Map<ResourceLocation, JsonObject>> loadModels(ResourceManager resourceManager, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            Map<ResourceLocation, JsonObject> models = new HashMap<>();

            for (Map.Entry<ResourceLocation, Resource> entry : resourceManager.listResources("models", path -> path.getPath().endsWith(".json")).entrySet()) {
                try (Reader reader = entry.getValue().openAsReader()) {
                    JsonElement json = JsonParser.parseReader(reader);

                    if (json.isJsonObject() && json.getAsJsonObject().has("loader") && LOADER.equals(json.getAsJsonObject().get("loader").getAsString())) {
                        String path = entry.getKey().getPath();
                        models.put(entry.getKey().withPath(path.substring("models/".length(), path.length() - ".json".length())), json.getAsJsonObject());
                    }
                } catch (Exception e) {
                    EpicFightMod.LOGGER.warn("Can't read model {}: {}", entry.getKey(), e.getMessage());
                }
            }

            return models;
        }, executor);
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        Set<ResourceLocation> dependencies = new HashSet<>(this.base.getDependencies());
        this.perspectives.values().forEach(model -> dependencies.addAll(model.getDependencies()));

        return dependencies;
    }

    @Override
    public void resolveParents(Function<ResourceLocation, UnbakedModel> resolver) {
        this.base.resolveParents(resolver);
        this.perspectives.values().forEach(model -> model.resolveParents(resolver));
    }

    @Override
    public BakedModel bake(ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState state) {
        BakedModel bakedBase = bakePart(this.base, baker, spriteGetter, state);
        Map<ItemDisplayContext, BakedModel> bakedPerspectives = new EnumMap<>(ItemDisplayContext.class);
        this.perspectives.forEach((displayContext, model) -> bakedPerspectives.put(displayContext, bakePart(model, baker, spriteGetter, state)));

        return new Baked(bakedBase, bakedPerspectives, this.usesBlockLight == null ? bakedBase.usesBlockLight() : this.usesBlockLight);
    }

    /// Same as `ModelBakery.ModelBakerImpl#bake`: `item/generated` models go through the item model generator.
    private static BakedModel bakePart(BlockModel model, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState state) {
        if (model.getRootModel() == ModelBakery.GENERATION_MARKER) {
            return ITEM_MODEL_GENERATOR.generateBlockModel(spriteGetter, model).bake(baker, model, spriteGetter, state, false);
        }

        return model.bake(baker, spriteGetter, state);
    }

    /// The baked base model, plus the model for each display context listed in `perspectives`.
    public static final class Baked implements BakedModel {
        private final BakedModel base;
        private final Map<ItemDisplayContext, BakedModel> perspectives;
        private final boolean usesBlockLight;

        private Baked(BakedModel base, Map<ItemDisplayContext, BakedModel> perspectives, boolean usesBlockLight) {
            this.base = base;
            this.perspectives = perspectives;
            this.usesBlockLight = usesBlockLight;
        }

        public BakedModel forDisplayContext(ItemDisplayContext displayContext) {
            return this.perspectives.getOrDefault(displayContext, this.base);
        }

        @Override
        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction direction, RandomSource random) {
            return this.base.getQuads(state, direction, random);
        }

        @Override
        public boolean useAmbientOcclusion() {
            return this.base.useAmbientOcclusion();
        }

        @Override
        public boolean isGui3d() {
            return this.base.isGui3d();
        }

        @Override
        public boolean usesBlockLight() {
            return this.usesBlockLight;
        }

        @Override
        public boolean isCustomRenderer() {
            return this.base.isCustomRenderer();
        }

        @Override
        public TextureAtlasSprite getParticleIcon() {
            return this.base.getParticleIcon();
        }

        @Override
        public ItemTransforms getTransforms() {
            return this.base.getTransforms();
        }

        @Override
        public ItemOverrides getOverrides() {
            return this.base.getOverrides();
        }
    }
}
