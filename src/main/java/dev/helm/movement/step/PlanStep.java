package dev.helm.movement.step;

import java.util.List;

import dev.helm.pathfinding.move.MoveKind;

public record PlanStep(int fromX, int fromY, int fromZ,
                       int toX, int toY, int toZ,
                       StepKind kind,
                       MoveKind move,
                       StepFootprint footprint,
                       boolean plannedWhileLoaded,
                       List<int[]> blocksToBreak,
                       int[] placeAt,
                       List<int[]> blocksToWalkInto) implements MoveStep {

    public PlanStep {
        blocksToBreak = List.copyOf(blocksToBreak);
        blocksToWalkInto = List.copyOf(blocksToWalkInto);
    }

    public static PlanStep of(int fromX, int fromY, int fromZ,
                              int toX, int toY, int toZ,
                              StepKind kind, MoveKind move, boolean plannedWhileLoaded,
                              List<int[]> blocksToBreak, int[] placeAt,
                              List<int[]> blocksToWalkInto) {
        PlanStep step = new PlanStep(fromX, fromY, fromZ, toX, toY, toZ, kind, move, null,
                plannedWhileLoaded, blocksToBreak, placeAt, blocksToWalkInto);
        return step.withFootprint(StepFootprint.of(step));
    }

    public static PlanStep to(int fromX, int fromY, int fromZ, int toX, int toY, int toZ) {
        return of(fromX, fromY, fromZ, toX, toY, toZ, StepKind.STEP, MoveKind.STEP_EAST,
                true, List.of(), null, List.of());
    }

    public PlanStep startingFrom(PlanStep previous) {
        return of(previous.toX(), previous.toY(), previous.toZ(), toX, toY, toZ, kind, move,
                plannedWhileLoaded, blocksToBreak, placeAt, blocksToWalkInto);
    }

    private PlanStep withFootprint(StepFootprint spots) {
        return new PlanStep(fromX, fromY, fromZ, toX, toY, toZ, kind, move, spots,
                plannedWhileLoaded, blocksToBreak, placeAt, blocksToWalkInto);
    }
}
