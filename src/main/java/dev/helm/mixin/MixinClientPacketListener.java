package dev.helm.mixin;

import dev.helm.command.CommandSystem;
import dev.helm.setting.Settings;
import dev.helm.world.cache.TrackedBlocks;
import dev.helm.world.cache.WorldCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundForgetLevelChunkPacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
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

    @Inject(method = "handleLevelChunkWithLight", at = @At("RETURN"))
    private void helmRememberChunk(ClientboundLevelChunkWithLightPacket packet,
                                   CallbackInfo callback) {
        remember(packet.getX(), packet.getZ());
    }

    @Inject(method = "handleForgetLevelChunk", at = @At("HEAD"))
    private void helmRememberChunkBeforeUnload(ClientboundForgetLevelChunkPacket packet,
                                                CallbackInfo callback) {
        remember(packet.pos().x(), packet.pos().z());
    }

    @Inject(method = "handleBlockUpdate", at = @At("RETURN"))
    private void helmRepackTrackedChange(ClientboundBlockUpdatePacket packet,
                                         CallbackInfo callback) {
        if (!Settings.holder().cache().repackOnBlockChange()) {
            return;
        }
        if (!TrackedBlocks.is(packet.getBlockState().getBlock())) {
            return;
        }
        remember(packet.getPos().getX() >> 4, packet.getPos().getZ() >> 4);
    }

    private static void remember(int chunkX, int chunkZ) {
        if (!Settings.holder().cache().enabled()) {
            return;
        }
        WorldCache cache = WorldCache.get();
        ClientLevel level = Minecraft.getInstance().level;
        if (cache == null || level == null) {
            return;
        }
        cache.remember(chunkX, chunkZ, level);
    }
}
