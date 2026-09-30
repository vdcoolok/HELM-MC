package dev.helm.mixin.accessor;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;

public interface GameModeAccess {

    boolean helmIsHitting();

    void helmSetHitting(boolean value);

    BlockPos helmCurrentTarget();

    void helmSetDestroyDelay(int value);

    void helmSyncCarriedItem();

    static GameModeAccess of(MultiPlayerGameMode mode) {
        return (GameModeAccess) mode;
    }

    static boolean hitting(MultiPlayerGameMode mode) {
        return of(mode).helmIsHitting();
    }

    static void hitting(MultiPlayerGameMode mode, boolean value) {
        of(mode).helmSetHitting(value);
    }

    static BlockPos currentTarget(MultiPlayerGameMode mode) {
        return of(mode).helmCurrentTarget();
    }

    static void destroyDelay(MultiPlayerGameMode mode, int value) {
        of(mode).helmSetDestroyDelay(value);
    }

    static void syncCarriedItem(MultiPlayerGameMode mode) {
        of(mode).helmSyncCarriedItem();
    }

    static void resetDestroying(MultiPlayerGameMode mode) {
        mode.stopDestroyBlock();
    }

    static boolean brokenBlock(MultiPlayerGameMode mode) {
        return !of(mode).helmIsHitting();
    }
}
