package dev.helm.movement.step;

import java.util.List;

import dev.helm.pathfinding.move.MoveKind;

public record PlanStep(int fromX, int fromY, int fromZ,
                       int toX, int toY, int toZ,
                       StepKind kind,
                       MoveKind move,
                       double cost,
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
                              StepKind kind, MoveKind move, double cost,
                              boolean plannedWhileLoaded,
                              List<int[]> blocksToBreak, int[] placeAt,
                              List<int[]> blocksToWalkInto) {
        PlanStep step = new PlanStep(fromX, fromY, fromZ, toX, toY, toZ, kind, move, cost, null,
                plannedWhileLoaded, blocksToBreak, placeAt, blocksToWalkInto);
        return step.withFootprint(StepFootprint.of(step));
    }

    public PlanStep startingFrom(PlanStep previous) {
        return of(previous.toX(), previous.toY(), previous.toZ(), toX, toY, toZ, kind, move,
                cost, plannedWhileLoaded, blocksToBreak, placeAt, blocksToWalkInto);
    }

    private PlanStep withFootprint(StepFootprint spots) {
        return new PlanStep(fromX, fromY, fromZ, toX, toY, toZ, kind, move, cost, spots,
                plannedWhileLoaded, blocksToBreak, placeAt, blocksToWalkInto);
    }
}
