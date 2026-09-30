package dev.helm.mixin;

import dev.helm.command.CommandSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class MixinClientPacketListener {

    @Inject(method = "sendChat", at = @At("HEAD"), cancellable = true)
    private void helmInterceptCommandLine(String message, CallbackInfo callback) {
        if (!CommandSystem.isCommandLine(message)) {
            return;
        }
        CommandSystem.dispatch(Minecraft.getInstance(), message);
        callback.cancel();
    }
}
