package dev.helm.access;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;

public interface GameModeControl {

    boolean hitting();

    void setHitting(boolean value);

    BlockPos currentTarget();

    void setDestroyDelay(int value);

    void syncCarriedItem();

    static GameModeControl of(MultiPlayerGameMode mode) {
        return (GameModeControl) mode;
    }

    static boolean hitting(MultiPlayerGameMode mode) {
        return of(mode).hitting();
    }

    static void setHitting(MultiPlayerGameMode mode, boolean value) {
        of(mode).setHitting(value);
    }

    static BlockPos currentTarget(MultiPlayerGameMode mode) {
        return of(mode).currentTarget();
    }

    static void setDestroyDelay(MultiPlayerGameMode mode, int value) {
        of(mode).setDestroyDelay(value);
    }

    static void syncCarriedItem(MultiPlayerGameMode mode) {
        of(mode).syncCarriedItem();
    }

    static void resetDestroying(MultiPlayerGameMode mode) {
        mode.stopDestroyBlock();
    }

    static boolean brokenBlock(MultiPlayerGameMode mode) {
        return !of(mode).hitting();
    }
}
