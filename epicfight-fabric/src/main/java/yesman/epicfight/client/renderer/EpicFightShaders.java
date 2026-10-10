package yesman.epicfight.client.renderer;

import java.io.IOException;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;

import net.fabricmc.fabric.impl.client.rendering.FabricShaderProgram;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.platform.client.event.RegisterShadersEvent;
import yesman.epicfight.main.EpicFightMod;

public class EpicFightShaders {
	public static ShaderInstance positionColorNormalShader;
	
	@Nullable
	public static ShaderInstance getPositionColorNormalShader() {
		return positionColorNormalShader;
	}
	
	/// Fabric port: registered in `EpicFightFabricClient`. `FabricShaderProgram` makes vanilla
	/// resolve the namespaced name (NeoForge patches `ShaderInstance` for the same).
	public static void registerShadersEvent(RegisterShadersEvent event) throws IOException {
		event.registerShader(new FabricShaderProgram(event.getResourceProvider(), EpicFightMod.identifier("solid_model"), DefaultVertexFormat.POSITION_COLOR_NORMAL), reloadedShader -> {
			positionColorNormalShader = reloadedShader;
		});
	}
}
