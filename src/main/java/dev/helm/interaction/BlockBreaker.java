package dev.helm.interaction;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.HitResult;

import dev.helm.access.GameModeControl;
import dev.helm.setting.Settings;

public final class BlockBreaker {

    private static final int BASE_DELAY = 1;

    private boolean wasHitting;
    private int delay;

    public void stop() {
        Minecraft client = Minecraft.getInstance();
        if (client.player != null && wasHitting) {
            GameModeControl.setHitting(client.gameMode, false);
            GameModeControl.resetDestroying(client.gameMode);
            wasHitting = false;
        }
    }

    public void tick(boolean wantsBreak) {
        if (delay > 0) {
            delay--;
            return;
        }
        Minecraft client = Minecraft.getInstance();
        MultiPlayerGameMode mode = client.gameMode;
        Player player = client.player;
        HitResult trace = client.hitResult;
        if (mode == null || player == null || client.level == null
                || !wantsBreak || trace == null || trace.getType() != HitResult.Type.BLOCK) {
            wasHitting = false;
            return;
        }

        BlockPos pos = ((net.minecraft.world.phys.BlockHitResult) trace).getBlockPos();
        Direction face = ((net.minecraft.world.phys.BlockHitResult) trace).getDirection();

        GameModeControl.setHitting(mode, wasHitting);
        if (GameModeControl.brokenBlock(mode)) {
            GameModeControl.syncCarriedItem(mode);
            mode.startDestroyBlock(pos, face);
            player.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
        } else {
            if (mode.continueDestroyBlock(pos, face)) {
                player.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
            }
            if (GameModeControl.brokenBlock(mode)) {
                delay = Settings.holder().movement().blockBreakSpeed() - BASE_DELAY;
                GameModeControl.setDestroyDelay(mode, 0);
            }
        }
        wasHitting = !GameModeControl.brokenBlock(mode);
        GameModeControl.setHitting(mode, false);
    }

    public boolean isBreaking() {
        return wasHitting;
    }
}
