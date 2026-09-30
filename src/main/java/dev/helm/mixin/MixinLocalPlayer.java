package dev.helm.mixin;

import dev.helm.control.SteeringInput;
import dev.helm.navigate.NavigatorAgent;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class MixinLocalPlayer {

    @Shadow
    public ClientInput input;

    @Inject(method = "tick", at = @At("TAIL"))
    private void helmApplySteering(CallbackInfo callback) {
        LocalPlayer player = (LocalPlayer) (Object) this;
        var controls = NavigatorAgent.instance().pilot().controls();
        if (controls.anyMovementRequested() || NavigatorAgent.instance().pilot().isWalking()) {
            if (!(player.input instanceof SteeringInput)) {
                player.input = new SteeringInput(controls);
            }
        } else if (player.input instanceof SteeringInput) {
            player.input = new KeyboardInput(net.minecraft.client.Minecraft.getInstance().options);
        }
    }
}
