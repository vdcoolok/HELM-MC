package dev.helm.movement.step;

import java.util.List;

import dev.helm.pathfinding.move.MoveKind;
import dev.helm.pathfinding.path.NodePath;
import dev.helm.pathfinding.world.BlockView;
import dev.helm.pathfinding.world.block.WalkRules;

public final class StepBuilder {

    private StepBuilder() {
    }

    public static PlanStep build(BlockView world, WalkRules walk, NodePath.Leg leg) {
        MoveKind move = leg.by();
        int toX = leg.toX();
        int toY = leg.toY();
        int toZ = leg.toZ();
        StepKind kind = kindFor(move, leg.fromY(), toY);

        List<int[]> breaks = StepBlocks.toBreak(world, walk,
                leg.fromX(), leg.fromY(), leg.fromZ(), toX, toY, toZ);
        int[] placement = placementFor(world, walk, kind, leg.fromX(), leg.fromY(), leg.fromZ(),
                toX, toY, toZ);
        List<int[]> walkInto = StepBlocks.walkInto(world, walk, leg.fromX(), leg.fromY(),
                leg.fromZ(), toX, toZ, kind == StepKind.LEAN);

        return new PlanStep(leg.fromX(), leg.fromY(), leg.fromZ(), toX, toY, toZ, kind,
                move, breaks, placement, walkInto);
    }

    private static StepKind kindFor(MoveKind move, int fromY, int toY) {
        StepKind kind = StepKind.of(move);
        if (kind == StepKind.DROP && toY != fromY - 1) {
            return StepKind.FALL;
        }
        return kind;
    }

    private static int[] placementFor(BlockView world, WalkRules walk, StepKind kind,
                                      int fromX, int fromY, int fromZ,
                                      int toX, int toY, int toZ) {
        return switch (kind) {
            case STEP -> StepBlocks.placeUnder(world, walk, toX, toY - 1, toZ);
            case STEP_UP -> StepBlocks.placeUnder(world, walk, toX, toY - 1, toZ);
            case RAISE -> StepBlocks.placeUnder(world, walk, fromX, fromY, fromZ);
            default -> null;
        };
    }
}
