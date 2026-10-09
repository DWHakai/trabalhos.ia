package yesman.epicfight.fabric;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import yesman.epicfight.EpicFight;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/// Fabric needs every reload listener to have an id, NeoForge does not. This wraps the
/// upstream listeners unchanged.
public record FabricReloadListener(ResourceLocation id, PreparableReloadListener delegate) implements IdentifiableResourceReloadListener {
    public static FabricReloadListener of(String name, PreparableReloadListener delegate) {
        return new FabricReloadListener(EpicFight.identifier(name), delegate);
    }

    @Override
    public ResourceLocation getFabricId() {
        return this.id;
    }

    @Override
    public CompletableFuture<Void> reload(PreparationBarrier barrier, ResourceManager resourceManager, ProfilerFiller preparationsProfiler, ProfilerFiller reloadProfiler, Executor backgroundExecutor, Executor gameExecutor) {
        return this.delegate.reload(barrier, resourceManager, preparationsProfiler, reloadProfiler, backgroundExecutor, gameExecutor);
    }

    @Override
    public String getName() {
        return this.delegate.getName();
    }
}
