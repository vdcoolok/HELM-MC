package dev.helm.mixin;

import dev.helm.aim.LookController;
import dev.helm.diag.TraceClock;
import dev.helm.diag.WorldReport;
import dev.helm.navigate.NavigatorAgent;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft {

    @Inject(method = "tick", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/multiplayer/ClientLevel;tickEntities()V",
            shift = At.Shift.AFTER))
    private void helmAfterEntities(CallbackInfo callback) {
        Minecraft client = (Minecraft) (Object) this;
        if (client.player == null || client.level == null) {
            return;
        }
        LookController.instance().afterPlayerUpdate();
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void helmTick(CallbackInfo callback) {
        TraceClock.advance();
        Minecraft client = (Minecraft) (Object) this;
        if (client.player == null || client.level == null) {
            return;
        }
        WorldReport.awaitPlayer();
        LookController.instance().tick();
        NavigatorAgent.instance().onTick();
    }
}
