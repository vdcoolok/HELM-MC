package dev.helm.farm;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import dev.helm.aim.Aim;
import dev.helm.aim.BlockReach;
import dev.helm.aim.LookController;
import dev.helm.setting.LookSettings;

public final class FarmHands {

    private FarmHands() {
    }

    public static boolean inReach(LocalPlayer player, BlockPos target, LookSettings look) {
        double reach = look.blockReachDistance();
        return player.blockPosition().distSqr(target) <= reach * reach;
    }

    public static Aim atBlock(LocalPlayer player, BlockPos target, LookSettings look) {
        return BlockReach.towards(player, target, look, false);
    }

    public static Aim atTopOf(LocalPlayer player, BlockPos target, LookSettings look) {
        return BlockReach.towardsPoint(player, target,
                new Vec3(target.getX() + 0.5D, target.getY() + 1.0D, target.getZ() + 0.5D),
                look, false);
    }

    public static Aim atSideOf(LocalPlayer player, BlockPos target, Direction side,
                               LookSettings look) {
        return BlockReach.towardsPoint(player, target,
                BlockReach.centre(target).add(side.getStepX() * 0.5D, side.getStepY() * 0.5D,
                        side.getStepZ() * 0.5D),
                look, false);
    }

    public static boolean facingSide(Aim aim, Direction face, LocalPlayer player,
                                     LookSettings look) {
        BlockHitResult trace = onBlock(player, aim, look);
        return trace != null && trace.getDirection() == face;
    }

    public static boolean handOnBlock(LocalPlayer player, BlockPos target, LookSettings look) {
        BlockHitResult trace = onBlock(player, new Aim(player.getYRot(), player.getXRot()),
                look, player.isCrouching());
        return trace != null && trace.getBlockPos().equals(target);
    }

    public static boolean crosshairOnBlock(LocalPlayer player, BlockPos target, LookSettings look) {
        Aim aim = LookController.instance().effective();
        if (aim == null) {
            return false;
        }
        BlockHitResult trace = onBlock(player, aim, look, player.isCrouching());
        return trace != null && trace.getBlockPos().equals(target);
    }

    private static BlockHitResult onBlock(LocalPlayer player, Aim aim, LookSettings look) {
        HitResult trace = BlockReach.trace(player, aim, look, false);
        return trace != null && trace.getType() == HitResult.Type.BLOCK
                ? (BlockHitResult) trace : null;
    }

    private static BlockHitResult onBlock(LocalPlayer player, Aim aim, LookSettings look,
                                          boolean sneaking) {
        HitResult trace = BlockReach.trace(player, aim, look, sneaking);
        return trace != null && trace.getType() == HitResult.Type.BLOCK
                ? (BlockHitResult) trace : null;
    }
}