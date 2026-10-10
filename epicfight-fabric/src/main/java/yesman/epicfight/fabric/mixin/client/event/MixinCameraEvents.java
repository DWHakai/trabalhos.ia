package yesman.epicfight.fabric.mixin.client.event;

import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import yesman.epicfight.platform.client.event.ViewportEvent;

/// NeoForge: `ViewportEvent.ComputeCameraAngles` in `Camera#setup`, and camera roll.
@Mixin(Camera.class)
public abstract class MixinCameraEvents {
    @Shadow @Final private static Vector3f FORWARDS;
    @Shadow @Final private static Vector3f UP;
    @Shadow @Final private static Vector3f LEFT;
    @Shadow @Final private Vector3f forwards;
    @Shadow @Final private Vector3f up;
    @Shadow @Final private Vector3f left;
    @Shadow @Final private Quaternionf rotation;

    @Shadow public abstract void setRotation(float yRot, float xRot);

    @Unique
    private float epicfight$roll;

    @Unique
    private void epicfight$setRotation(float yRot, float xRot, float roll) {
        this.setRotation(yRot, xRot);
        this.epicfight$roll = roll;

        if (roll != 0.0F) {
            this.rotation.rotationYXZ((float)Math.PI - yRot * (float)(Math.PI / 180.0), -xRot * (float)(Math.PI / 180.0), -roll * (float)(Math.PI / 180.0));
            FORWARDS.rotate(this.rotation, this.forwards);
            UP.rotate(this.rotation, this.up);
            LEFT.rotate(this.rotation, this.left);
        }
    }

    @Redirect(method = "setup", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setRotation(FF)V", ordinal = 0))
    private void epicfight$computeCameraAngles(Camera camera, float yRot, float xRot, BlockGetter level, Entity entity, boolean detached, boolean thirdPersonReverse, float partialTick) {
        ViewportEvent.ComputeCameraAngles event = ViewportEvent.ComputeCameraAngles.BUS.post(new ViewportEvent.ComputeCameraAngles(camera, partialTick, yRot, xRot, 0.0F));
        this.epicfight$setRotation(event.getYaw(), event.getPitch(), event.getRoll());
    }

    @Redirect(method = "setup", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setRotation(FF)V", ordinal = 1))
    private void epicfight$reverseRotation(Camera camera, float yRot, float xRot) {
        this.epicfight$setRotation(yRot, xRot, -this.epicfight$roll);
    }

    @Redirect(method = "setup", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setRotation(FF)V", ordinal = 2))
    private void epicfight$sleepingRotation(Camera camera, float yRot, float xRot) {
        this.epicfight$setRotation(yRot, xRot, 0.0F);
    }
}
