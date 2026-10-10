package yesman.epicfight.platform.client.event;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.server.packs.resources.ResourceProvider;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/// NeoForge `RegisterShadersEvent`, fired while `GameRenderer#reloadShaders` (re)creates the core
/// shaders: listeners create their `ShaderInstance`s from the resource provider and get them back
/// through the callback once vanilla installs the new shaders.
public class RegisterShadersEvent {
    private static final List<Listener> LISTENERS = new CopyOnWriteArrayList<>();

    private final ResourceProvider resourceProvider;
    private final List<Pair<ShaderInstance, Consumer<ShaderInstance>>> shaderList;

    public RegisterShadersEvent(ResourceProvider resourceProvider, List<Pair<ShaderInstance, Consumer<ShaderInstance>>> shaderList) {
        this.resourceProvider = resourceProvider;
        this.shaderList = shaderList;
    }

    public ResourceProvider getResourceProvider() {
        return this.resourceProvider;
    }

    public void registerShader(ShaderInstance shaderInstance, Consumer<ShaderInstance> onLoaded) {
        this.shaderList.add(Pair.of(shaderInstance, onLoaded));
    }

    public static void addListener(Listener listener) {
        LISTENERS.add(listener);
    }

    public static void post(RegisterShadersEvent event) throws IOException {
        for (Listener listener : LISTENERS) {
            listener.onRegisterShaders(event);
        }
    }

    @FunctionalInterface
    public interface Listener {
        void onRegisterShaders(RegisterShadersEvent event) throws IOException;
    }
}
