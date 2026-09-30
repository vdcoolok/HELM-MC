package dev.helm.movement.step;

import java.util.LinkedHashSet;
import java.util.Set;

import net.minecraft.world.level.block.Blocks;

import dev.helm.control.Control;
import dev.helm.movement.MoveState;
import dev.helm.movement.MoveTick;
import dev.helm.pathfinding.world.block.LiquidRules;

public final class LeanStepExecutor implements StepExecutor {

    @Override
    public MoveState advance(StepContext context, MoveTick tick, PlanStep step) {
        if (!StepPreparation.ready(context, tick, step)) {
            return MoveState.PREPPING;
        }
        StepPreparation.advanceStatus(tick);
        if (tick.state() != MoveState.RUNNING) {
            return tick.state();
        }
        int[] feet = context.feet();
        if (feet[0] == step.toX() && feet[1] == step.toY() && feet[2] == step.toZ()) {
            return MoveState.SUCCESS;
        }
        if (!valid(context, step)) {
            return MoveState.UNREACHABLE;
        }
        if (step.toY() > step.fromY()
                && context.player().getY() < step.fromY() + 0.1D
                && context.player().horizontalCollision) {
            tick.press(Control.JUMP);
        }
        if (canSprint(context, step)) {
            tick.press(Control.SPRINT);
        }
        tick.set(Control.SNEAK, context.movement().magmaWalkAllowed()
                && context.world().stateAt(feet[0], feet[1] - 1, feet[2]).getBlock()
                        == Blocks.MAGMA_BLOCK);
        StepPreparation.walkTowards(context, tick, step.toX(), step.toY(), step.toZ());
        return MoveState.RUNNING;
    }

    private boolean canSprint(StepContext context, PlanStep step) {
        int[] feet = context.feet();
        if (LiquidRules.any(context.world().stateAt(feet[0], feet[1], feet[2]))
                && !context.movement().sprintInWater()) {
            return false;
        }
        for (int[] position : step.blocksToWalkInto()) {
            if (!context.walk().through(position[0], position[1], position[2])) {
                return false;
            }
        }
        return true;
    }

    private boolean valid(StepContext context, PlanStep step) {
        Set<int[]> spots = validSpots(step);
        for (int[] spot : spots) {
            int[] feet = context.feet();
            if (feet[0] == spot[0] && feet[1] == spot[1] && feet[2] == spot[2]) {
                return true;
            }
        }
        return false;
    }

    private Set<int[]> validSpots(PlanStep step) {
        Set<int[]> spots = new LinkedHashSet<>();
        int diagAX = step.fromX();
        int diagAZ = step.toZ();
        int diagBX = step.toX();
        int diagBZ = step.fromZ();
        spots.add(new int[]{step.fromX(), step.fromY(), step.fromZ()});
        if (step.toY() < step.fromY()) {
            spots.add(new int[]{step.toX(), step.toY() + 1, step.toZ()});
            spots.add(new int[]{step.toX(), step.toY(), step.toZ()});
            spots.add(new int[]{diagAX, step.fromY() - 1, diagAZ});
            spots.add(new int[]{diagBX, step.fromY() - 1, diagBZ});
        } else if (step.toY() > step.fromY()) {
            spots.add(new int[]{step.fromX(), step.fromY() + 1, step.fromZ()});
            spots.add(new int[]{step.toX(), step.toY(), step.toZ()});
            spots.add(new int[]{diagAX, step.fromY() + 1, diagAZ});
            spots.add(new int[]{diagBX, step.fromY() + 1, diagBZ});
        } else {
            spots.add(new int[]{step.toX(), step.toY(), step.toZ()});
        }
        spots.add(new int[]{diagAX, step.fromY(), diagAZ});
        spots.add(new int[]{diagBX, step.fromY(), diagBZ});
        return spots;
    }
}
