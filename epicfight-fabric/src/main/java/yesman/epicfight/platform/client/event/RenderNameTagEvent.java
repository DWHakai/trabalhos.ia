package yesman.epicfight.platform.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import yesman.epicfight.platform.event.ListenerList;

/// NeoForge `RenderNameTagEvent`. On Fabric it is only posted by Epic Fight's own patched
/// renderers (vanilla renderers keep the vanilla name tag logic).
public class RenderNameTagEvent {
    public static final ListenerList<RenderNameTagEvent> BUS = new ListenerList<>();

    private final Entity entity;
    private final Component originalContent;
    private final EntityRenderer<?> entityRenderer;
    private final PoseStack poseStack;
    private final MultiBufferSource multiBufferSource;
    private final int packedLight;
    private final float partialTick;
    private Component nameplateContent;
    private TriState canRender = TriState.DEFAULT;

    public RenderNameTagEvent(Entity entity, Component content, EntityRenderer<?> entityRenderer, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight, float partialTick) {
        this.entity = entity;
        this.originalContent = content;
        this.nameplateContent = content;
        this.entityRenderer = entityRenderer;
        this.poseStack = poseStack;
        this.multiBufferSource = multiBufferSource;
        this.packedLight = packedLight;
        this.partialTick = partialTick;
    }

    public Entity getEntity() {
        return this.entity;
    }

    public void setCanRender(TriState canRender) {
        this.canRender = canRender;
    }

    public TriState canRender() {
        return this.canRender;
    }

    public void setContent(Component contents) {
        this.nameplateContent = contents;
    }

    public Component getContent() {
        return this.nameplateContent;
    }

    public Component getOriginalContent() {
        return this.originalContent;
    }

    public EntityRenderer<?> getEntityRenderer() {
        return this.entityRenderer;
    }

    public PoseStack getPoseStack() {
        return this.poseStack;
    }

    public MultiBufferSource getMultiBufferSource() {
        return this.multiBufferSource;
    }

    public int getPackedLight() {
        return this.packedLight;
    }

    public float getPartialTick() {
        return this.partialTick;
    }
}
