package dev.helm.movement.step;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import dev.helm.aim.Aim;
import dev.helm.aim.Aiming;
import dev.helm.aim.AimTrace;
import dev.helm.aim.BlockReach;
import dev.helm.control.Control;
import dev.helm.movement.MoveIntent;
import dev.helm.movement.MoveState;
import dev.helm.movement.MoveTick;
import dev.helm.tools.ToolChooser;

public final class StepPreparation {

    private StepPreparation() {
    }

    public static boolean ready(StepContext context, MoveTick tick, PlanStep step) {
        if (tick.state() == MoveState.WAITING) {
            return true;
        }
        boolean obstructed = false;
        for (int[] position : step.blocksToBreak()) {
            BlockPos pos = new BlockPos(position[0], position[1], position[2]);
            if (context.movement().pauseMiningForFallingBlocks() && stillFalling(context, pos)) {
                return false;
            }
            if (context.walk().through(pos.getX(), pos.getY(), pos.getZ())) {
                continue;
            }
            obstructed = true;
            ToolChooser.forBlock(pos);
            Aim reachable = BlockReach.towards(context.player(), pos, context.look(), false);
            if (reachable != null) {
                tick.intent().aimedAt(reachable, true);
                if (lookingAt(context, pos) || BlockReach.current(context.player()).near(reachable)) {
                    tick.press(Control.ATTACK);
                }
                return false;
            }
            tick.intent().aimedAt(centeredOn(context, pos), true);
            tick.press(Control.ATTACK);
            return false;
        }
        if (obstructed) {
            tick.state(MoveState.UNREACHABLE);
        }
        return true;
    }

    public static void advanceStatus(MoveTick tick) {
        if (tick.state() == MoveState.PREPPING) {
            tick.state(MoveState.WAITING);
        }
        if (tick.state() == MoveState.WAITING) {
            tick.state(MoveState.RUNNING);
        }
    }

    public static void walkTowards(StepContext context, MoveTick tick, int x, int y, int z) {
        Aim aimed = centeredOn(context, new BlockPos(x, y, z));
        tick.intent().aimedAt(aimed.withPitch(context.player().getXRot()), false);
        tick.press(Control.MOVE_FORWARD);
    }

    public static Aim centeredOn(StepContext context, BlockPos pos) {
        Aim facing = new Aim(context.player().getYRot(), context.player().getXRot());
        return Aiming.shortestFrom(facing, Aiming.towardBlock(context.player(), pos));
    }

    private static Aim facing(StepContext context) {
        return new Aim(context.player().getYRot(), context.player().getXRot());
    }

    private static boolean lookingAt(StepContext context, BlockPos pos) {
        var player = context.player();
        HitResult trace = AimTrace.towards(player, new Aim(player.getYRot(), player.getXRot()),
                context.look().blockReachDistance(), player.isCrouching());
        return trace != null && trace.getType() == HitResult.Type.BLOCK
                && ((BlockHitResult) trace).getBlockPos().equals(pos);
    }

    private static boolean stillFalling(StepContext context, BlockPos pos) {
        return !context.player().level().getEntitiesOfClass(FallingBlockEntity.class,
                new AABB(pos).inflate(0.1D)).isEmpty();
    }

    public static MoveIntent intentOf(MoveTick tick) {
        return tick.intent();
    }
}
