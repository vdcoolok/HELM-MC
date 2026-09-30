package dev.helm.mixin;

import dev.helm.aim.LookController;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntityJumpRotation {

    @Inject(method = "jumpFromGround", at = @At("HEAD"))
    private void helmBeginJump(CallbackInfo callback) {
        if (((Object) this) instanceof LocalPlayer) {
            LookController.instance().beginJump();
        }
    }

    @Redirect(method = "jumpFromGround", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;getYRot()F"))
    private float helmJumpYaw(LivingEntity self) {
        if (((Object) self) instanceof LocalPlayer) {
            Float yaw = LookController.instance().jumpYaw();
            if (yaw != null) {
                return yaw;
            }
        }
        return self.getYRot();
    }

    @Inject(method = "jumpFromGround", at = @At("RETURN"))
    private void helmEndJump(CallbackInfo callback) {
        if (((Object) this) instanceof LocalPlayer) {
            LookController.instance().endJump();
        }
    }
}
