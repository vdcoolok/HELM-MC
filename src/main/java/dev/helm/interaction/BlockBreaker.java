package dev.helm.interaction;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import dev.helm.access.GameModeControl;
import dev.helm.aim.Aim;
import dev.helm.aim.AimTrace;
import dev.helm.aim.LookController;
import dev.helm.diag.Trace;
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
        if (mode == null || player == null || client.level == null || !wantsBreak) {
            wasHitting = false;
            return;
        }

        BlockHitResult trace = aimed(player);
        if (trace == null) {
            wasHitting = false;
            return;
        }
        BlockPos pos = trace.getBlockPos();
        Direction face = trace.getDirection();
        boolean accepted;

        GameModeControl.setHitting(mode, wasHitting);
        if (GameModeControl.brokenBlock(mode)) {
            GameModeControl.syncCarriedItem(mode);
            accepted = mode.startDestroyBlock(pos, face);
            player.swing(InteractionHand.MAIN_HAND);
        } else {
            accepted = true;
            if (mode.continueDestroyBlock(pos, face)) {
                player.swing(InteractionHand.MAIN_HAND);
            }
            if (GameModeControl.brokenBlock(mode)) {
                delay = Settings.holder().movement().blockBreakSpeed() - BASE_DELAY;
                GameModeControl.setDestroyDelay(mode, 0);
            }
        }
        Trace.instance().repeat("mine", "mine", "working on " + pos + " "
                + client.level.getBlockState(pos).getBlock().getName().getString()
                + " holding " + describe(player) + ", the game took it " + accepted);
        wasHitting = !GameModeControl.brokenBlock(mode);
        GameModeControl.setHitting(mode, false);
    }

    private static String describe(Player player) {
        var held = player.getMainHandItem();
        return "slot " + player.getInventory().getSelectedSlot() + " "
                + (held.isEmpty() ? "nothing" : held.getHoverName().getString());
    }

    private static BlockHitResult aimed(Player player) {
        Aim aim = LookController.instance().effective();
        if (aim == null) {
            return null;
        }
        HitResult trace = AimTrace.towards(player, aim,
                Settings.holder().look().blockReachDistance(), player.isCrouching());
        if (trace == null || trace.getType() != HitResult.Type.BLOCK) {
            return null;
        }
        return (BlockHitResult) trace;
    }

    public boolean isBreaking() {
        return wasHitting;
    }
}