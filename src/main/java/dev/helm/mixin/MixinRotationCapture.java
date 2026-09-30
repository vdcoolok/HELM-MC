package dev.helm.mixin;

import dev.helm.aim.LookController;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientCommonPacketListenerImpl.class)
public abstract class MixinRotationCapture {

    @Inject(method = "send", at = @At("HEAD"))
    private void helmRecordSentRotation(Packet<?> packet, CallbackInfo callback) {
        if (!(packet instanceof ServerboundMovePlayerPacket move)) {
            return;
        }
        if (packet instanceof ServerboundMovePlayerPacket.Rot
                || packet instanceof ServerboundMovePlayerPacket.PosRot) {
            LookController.instance().onServerRotation(move.getYRot(0.0F), move.getXRot(0.0F));
        }
    }
}
