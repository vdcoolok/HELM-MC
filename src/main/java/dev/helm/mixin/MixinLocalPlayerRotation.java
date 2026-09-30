package dev.helm.mixin;

import dev.helm.aim.LookController;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class MixinLocalPlayerRotation {

    @Inject(method = "sendPosition", at = @At("HEAD"), cancellable = true)
    private void helmInjectRotation(CallbackInfo callback) {
        var aim = LookController.instance().forMovementPacket();
        if (aim == null) {
            return;
        }
        LocalPlayer player = (LocalPlayer) (Object) this;
        player.setYRot((float) aim.yaw());
        player.setXRot((float) aim.pitch());
    }
}
