package dev.helm.movement.step;

import java.util.List;

import dev.helm.pathfinding.move.MoveKind;

public record PlanStep(int fromX, int fromY, int fromZ,
                       int toX, int toY, int toZ,
                       StepKind kind,
                       MoveKind move,
                       List<int[]> blocksToBreak,
                       int[] placeAt,
                       List<int[]> blocksToWalkInto) implements MoveStep {

    public PlanStep {
        blocksToBreak = List.copyOf(blocksToBreak);
        blocksToWalkInto = List.copyOf(blocksToWalkInto);
    }

    public static PlanStep to(int fromX, int fromY, int fromZ, int toX, int toY, int toZ) {
        return new PlanStep(fromX, fromY, fromZ, toX, toY, toZ, StepKind.STEP,
                MoveKind.STEP_EAST, List.of(), null, List.of());
    }
}
