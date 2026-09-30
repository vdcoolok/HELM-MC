package dev.helm.movement.step;

import dev.helm.pathfinding.move.MoveKind;

public enum StepKind {

    STEP,
    STEP_UP,
    DROP,
    FALL,
    LEAN,
    RAISE,
    SINK,
    BOUND;

    public static StepKind of(MoveKind move) {
        return switch (move) {
            case STEP_NORTH, STEP_SOUTH, STEP_EAST, STEP_WEST -> STEP;
            case STEP_UP_NORTH, STEP_UP_SOUTH, STEP_UP_EAST, STEP_UP_WEST -> STEP_UP;
            case DROP_NORTH, DROP_SOUTH, DROP_EAST, DROP_WEST -> DROP;
            case LEAN_NORTHEAST, LEAN_NORTHWEST, LEAN_SOUTHEAST, LEAN_SOUTHWEST -> LEAN;
            case RAISE -> RAISE;
            case DROP -> SINK;
            case BOUND_NORTH, BOUND_SOUTH, BOUND_EAST, BOUND_WEST -> BOUND;
        };
    }
}
