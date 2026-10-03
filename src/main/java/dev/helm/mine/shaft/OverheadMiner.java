package dev.helm.mine.shaft;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;

import dev.helm.aim.Aim;
import dev.helm.aim.BlockReach;
import dev.helm.aim.LookController;
import dev.helm.control.Control;
import dev.helm.navigate.Pilot;
import dev.helm.setting.LookSettings;

public final class OverheadMiner {

    private OverheadMiner() {
    }

    public static boolean work(Pilot pilot, OverheadSpot spot, LocalPlayer player,
                               LookSettings look) {
        if (BlockReach.reachableFrom(player, spot.block(), look.blockReachDistance(),
                player.isCrouching()) == null) {
            return false;
        }
        Aim aim = BlockReach.towards(player, spot.block(), look, false);
        if (aim == null) {
            return false;
        }
        dev.helm.tools.ToolChooser.forBlock(spot.block());
        LookController.instance().aimAt(aim, true);
        if (!onBlock(player, spot.block(), look)) {
            return false;
        }
        pilot.controls().set(Control.ATTACK, true);
        return true;
    }

    private static boolean onBlock(LocalPlayer player, BlockPos block, LookSettings look) {
        Aim held = LookController.instance().effective();
        if (held == null) {
            return false;
        }
        var trace = BlockReach.trace(player, held, look, player.isCrouching());
        return trace != null
                && trace.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK
                && ((net.minecraft.world.phys.BlockHitResult) trace).getBlockPos().equals(block);
    }
}