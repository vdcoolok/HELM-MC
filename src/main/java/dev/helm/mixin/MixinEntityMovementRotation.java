package dev.helm.mixin;

import dev.helm.aim.LookController;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class MixinEntityMovementRotation {

    @Shadow
    private float yRot;

    @Shadow
    private float xRot;

    @Unique
    private float helmSavedYaw;

    @Unique
    private float helmSavedPitch;

    @Unique
    private boolean helmSwapped;

    @Inject(method = "moveRelative", at = @At("HEAD"))
    private void helmAimBeforeMovement(float partialTick, Vec3 input,
                                        CallbackInfo callback) {
        if (!(((Object) this) instanceof LocalPlayer)) {
            return;
        }
        swapToAim();
    }

    @Inject(method = "moveRelative", at = @At("RETURN"))
    private void helmRestoreAfterMovement(float partialTick, Vec3 input,
                                          CallbackInfo callback) {
        restore();
    }

    @Unique
    private void swapToAim() {
        LookController look = LookController.instance();
        if (!look.hasMovementAim()) {
            return;
        }
        var aim = look.movementAim();
        if (aim == null) {
            return;
        }
        helmSavedYaw = yRot;
        helmSavedPitch = xRot;
        yRot = (float) aim.yaw();
        xRot = (float) aim.pitch();
        helmSwapped = true;
    }

    @Unique
    private void restore() {
        if (!helmSwapped) {
            return;
        }
        yRot = helmSavedYaw;
        xRot = helmSavedPitch;
        helmSwapped = false;
    }
}
