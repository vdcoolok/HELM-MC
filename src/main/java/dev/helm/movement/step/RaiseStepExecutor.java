package dev.helm.movement.step;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.AirBlock;

import dev.helm.aim.Aim;
import dev.helm.aim.BlockReach;
import dev.helm.control.Control;
import dev.helm.movement.MoveState;
import dev.helm.movement.MoveTick;
import dev.helm.pathfinding.world.block.Climbable;
import dev.helm.pathfinding.world.block.LiquidRules;
import dev.helm.tools.ThrowawayChooser;

public final class RaiseStepExecutor implements StepExecutor {

    private static final double CENTRE_TOLERANCE = 0.2D;
    private static final double ALIGN_TOLERANCE = 0.17D;
    private static final double FLAT_MOTION = 0.05D;
    private static final double ABOVE_DEST = 0.1D;

    @Override
    public MoveState advance(StepContext context, MoveTick tick, PlanStep step) {
        PillarPreparation.apply(context, tick, step);
        if (!PillarPreparation.alreadySwimming(context, step)
                && !StepPreparation.ready(context, tick, step)) {
            return MoveState.PREPPING;
        }
        StepPreparation.advanceStatus(tick);
        if (tick.state() != MoveState.RUNNING) {
            return tick.state();
        }
        int[] feet = context.feet();
        if (feet[1] < step.fromY()) {
            return MoveState.UNREACHABLE;
        }

        if (LiquidRules.water(context.world().stateAt(step.fromX(), step.fromY() - 1, step.fromZ()))
                && LiquidRules.water(context.world().stateAt(step.toX(), step.toY(), step.toZ()))) {
            return swimUp(context, tick, step);
        }

        boolean climbing = Climbable.is(context.world()
                .stateAt(step.fromX(), step.fromY(), step.fromZ()).getBlock());
        BlockPos toPlace = step.placeAt() == null
                ? new BlockPos(step.fromX(), step.fromY(), step.fromZ())
                : new BlockPos(step.placeAt()[0], step.placeAt()[1], step.placeAt()[2]);
        Aim aim = StepPreparation.centeredOn(context, toPlace);
        if (!climbing) {
            tick.intent().aimedAt(aim.withYaw(context.player().getYRot()), true);
        }

        boolean groundThere = context.walk().onTop(step.fromX(), step.fromY(), step.fromZ());
        if (climbing) {
            if (atDest(context, step)) {
                return MoveState.SUCCESS;
            }
            StepPreparation.walkTowards(context, tick, step.toX(), step.toY(), step.toZ());
            tick.press(Control.JUMP);
            return MoveState.RUNNING;
        }

        if (!ThrowawayChooser.selectFor(true)) {
            return MoveState.UNREACHABLE;
        }
        tick.press(Control.SNEAK);

        double offsetX = context.player().getX() - (step.toX() + 0.5D);
        double offsetZ = context.player().getZ() - (step.toZ() + 0.5D);
        double distance = Math.sqrt(offsetX * offsetX + offsetZ * offsetZ);
        double flatMotion = Math.sqrt(
                context.player().getDeltaMovement().x * context.player().getDeltaMovement().x
                        + context.player().getDeltaMovement().z
                        * context.player().getDeltaMovement().z);
        if (distance > ALIGN_TOLERANCE) {
            tick.press(Control.MOVE_FORWARD);
            tick.intent().aimedAt(aim, true);
        } else if (flatMotion < FLAT_MOTION) {
            tick.set(Control.JUMP, context.player().getY() < step.toY());
        }
        if (!groundThere) {
            clearColumn(context, tick, step, toPlace);
        }
        if (atDest(context, step) && (groundThere || climbing)) {
            return MoveState.SUCCESS;
        }
        return MoveState.RUNNING;
    }

    private void clearColumn(StepContext context, MoveTick tick, PlanStep step, BlockPos toPlace) {
        var state = context.world().stateAt(toPlace.getX(), toPlace.getY(), toPlace.getZ());
        if (state.getBlock() instanceof AirBlock || state.canBeReplaced()) {
            if (context.player().isCrouching()
                    && (lookingAt(context, toPlace.below()) || lookingAt(context, toPlace))
                    && context.player().getY() > step.toY() + ABOVE_DEST) {
                tick.press(Control.USE);
            }
            return;
        }
        Aim reachable = BlockReach.towards(context.player(), toPlace, context.look(), false);
        if (reachable != null) {
            tick.intent().aimedAt(reachable, true);
        }
        tick.set(Control.JUMP, false);
        tick.press(Control.ATTACK);
    }

    private MoveState swimUp(StepContext context, MoveTick tick, PlanStep step) {
        tick.intent().aimedAt(StepPreparation.centeredOn(context,
                new BlockPos(step.toX(), step.toY(), step.toZ())), false);
        var centre = BlockReach.centre(new BlockPos(step.toX(), step.toY(), step.toZ()));
        if (Math.abs(context.player().getX() - centre.x) > CENTRE_TOLERANCE
                || Math.abs(context.player().getZ() - centre.z) > CENTRE_TOLERANCE) {
            tick.press(Control.MOVE_FORWARD);
        }
        if (atDest(context, step)) {
            return MoveState.SUCCESS;
        }
        return MoveState.RUNNING;
    }

    private boolean atDest(StepContext context, PlanStep step) {
        int[] feet = context.feet();
        return feet[0] == step.toX() && feet[1] == step.toY() && feet[2] == step.toZ();
    }

    private boolean lookingAt(StepContext context, BlockPos pos) {
        var player = context.player();
        var trace = dev.helm.aim.AimTrace.towards(player,
                new dev.helm.aim.Aim(player.getYRot(), player.getXRot()),
                context.look().blockReachDistance(), player.isCrouching());
        return trace != null
                && trace.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK
                && ((net.minecraft.world.phys.BlockHitResult) trace).getBlockPos().equals(pos);
    }
}
