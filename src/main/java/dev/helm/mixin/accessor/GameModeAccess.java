package dev.helm.mixin.accessor;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MultiPlayerGameMode.class)
public interface GameModeAccess {

    @Accessor("isDestroying")
    boolean helmIsHitting(MultiPlayerGameMode mode);

    @Accessor("isDestroying")
    void helmSetHitting(MultiPlayerGameMode mode, boolean value);

    @Accessor("destroyBlockPos")
    BlockPos helmCurrentTarget(MultiPlayerGameMode mode);

    @Accessor("destroyDelay")
    void helmSetDestroyDelay(MultiPlayerGameMode mode, int value);

    @Invoker("ensureHasSentCarriedItem")
    void helmSyncCarriedItem(MultiPlayerGameMode mode);

    static void hitting(MultiPlayerGameMode mode, boolean value) {
        ((GameModeAccess) mode).helmSetHitting(mode, value);
    }

    static boolean hitting(MultiPlayerGameMode mode) {
        return ((GameModeAccess) mode).helmIsHitting(mode);
    }

    static void resetDestroying(MultiPlayerGameMode mode) {
        mode.stopDestroyBlock();
    }

    static void destroyDelay(MultiPlayerGameMode mode, int value) {
        ((GameModeAccess) mode).helmSetDestroyDelay(mode, value);
    }

    static void syncCarriedItem(MultiPlayerGameMode mode) {
        ((GameModeAccess) mode).helmSyncCarriedItem(mode);
    }

    static boolean brokenBlock(MultiPlayerGameMode mode) {
        return !((GameModeAccess) mode).helmIsHitting(mode);
    }
}
