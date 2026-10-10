package yesman.epicfight.platform.client.event;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/// NeoForge `RegisterGuiLayersEvent#registerAboveAll`: layers drawn on top of the vanilla HUD,
/// in registration order and only while the GUI is shown. `MixinGuiEvents` appends [#render] to
/// the layers of `Gui`.
public final class GuiLayers {
    private static final List<Pair<ResourceLocation, LayeredDraw.Layer>> ABOVE_ALL = new CopyOnWriteArrayList<>();

    public static void registerAboveAll(ResourceLocation id, LayeredDraw.Layer layer) {
        ABOVE_ALL.add(Pair.of(id, layer));
    }

    public static void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        for (Pair<ResourceLocation, LayeredDraw.Layer> layer : ABOVE_ALL) {
            // Same spacing as the layers of LayeredDraw so each one renders above the previous
            guiGraphics.pose().pushPose();
            layer.getSecond().render(guiGraphics, deltaTracker);
            guiGraphics.pose().popPose();
            guiGraphics.pose().translate(0.0F, 0.0F, LayeredDraw.Z_SEPARATION);
        }
    }

    private GuiLayers() {}
}
