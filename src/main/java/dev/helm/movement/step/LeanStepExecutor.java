package dev.helm.movement.step;

import net.minecraft.world.level.block.Blocks;

import dev.helm.control.Control;
import dev.helm.movement.MoveState;
import dev.helm.movement.MoveTick;
import dev.helm.pathfinding.world.block.LiquidRules;

public final class LeanStepExecutor implements StepExecutor {

    @Override
    public MoveState advance(StepContext context, MoveTick tick, PlanStep step) {
        TraversePreparation.apply(context, tick, step);
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
        if (!onRoute(step, feet)) {
            return wrongHeight(context, tick, step, feet);
        }
        if (step.toY() > step.fromY()
                && context.player().getY() < step.fromY() + 0.1D
                && context.player().horizontalCollision) {
            tick.press(Control.JUMP);
        }
        if (canSprint(context, step, feet)) {
            tick.press(Control.SPRINT);
        }
        tick.set(Control.SNEAK, context.movement().magmaWalkAllowed()
                && context.world().stateAt(feet[0], feet[1] - 1, feet[2]).getBlock()
                        == Blocks.MAGMA_BLOCK);
        StepPreparation.walkTowards(context, tick, step.toX(), step.toY(), step.toZ());
        return MoveState.RUNNING;
    }

    private boolean canSprint(StepContext context, PlanStep step, int[] feet) {
        if (LiquidRules.any(context.world().stateAt(feet[0], feet[1], feet[2]))
                && !context.movement().sprintInWater()) {
            return false;
        }
        return StepBlocks.walkInto(context.world(), context.walk(), step.fromX(), step.fromY(),
                step.fromZ(), step.toX(), step.toZ(), true).isEmpty();
    }

    private boolean onRoute(PlanStep step, int[] feet) {
        return step.footprint().contains(feet[0], feet[1], feet[2]);
    }

    private MoveState wrongHeight(StepContext context, MoveTick tick, PlanStep step, int[] feet) {
        StepPreparation.walkTowards(context, tick, step.toX(), step.toY(), step.toZ());
        if (feet[1] < step.toY()) {
            tick.press(Control.JUMP);
        }
        return MoveState.RUNNING;
    }

    @Override
    public boolean safeToCancel(StepContext context, PlanStep step, MoveTick tick) {
        int[] feet = context.feet();
        if (feet[0] == step.fromX() && feet[1] == step.fromY() && feet[2] == step.fromZ()) {
            return true;
        }
        int fromY = step.fromY();
        boolean cornerA = context.walk().onTop(step.fromX(), fromY - 1, step.toZ());
        boolean cornerB = context.walk().onTop(step.toX(), fromY - 1, step.fromZ());
        if (cornerA && cornerB) {
            return true;
        }
        boolean inCorner = feet[0] == step.fromX() && feet[1] == fromY && feet[2] == step.toZ()
                || feet[0] == step.toX() && feet[1] == fromY && feet[2] == step.fromZ();
        if (!inCorner) {
            return true;
        }
        int x = (int) context.player().getX();
        int y = (int) (context.player().getY() - 1);
        int z = (int) context.player().getZ();
        return context.walk().onTop(x + 1, y, z + 1)
                || context.walk().onTop(x + 1, y, z - 1)
                || context.walk().onTop(x - 1, y, z + 1)
                || context.walk().onTop(x - 1, y, z - 1);
    }
}
