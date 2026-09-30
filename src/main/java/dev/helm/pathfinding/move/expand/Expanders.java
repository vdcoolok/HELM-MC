package dev.helm.pathfinding.move.expand;

import dev.helm.pathfinding.move.MoveEnvironment;
import dev.helm.pathfinding.move.MoveExpander;
import dev.helm.pathfinding.move.MoveKind;

public final class Expanders {

    private Expanders() {
    }

    public static MoveExpander[] forEnvironment(MoveEnvironment env) {
        MoveExpander[] expanders = new MoveExpander[MoveKind.values().length];
        StepExpander step = new StepExpander(env);
        StepUpExpander stepUp = new StepUpExpander(env);
        DropExpander drop = new DropExpander(env);
        LeanExpander lean = new LeanExpander(env);
        RaiseExpander raise = new RaiseExpander(env);
        SinkExpander sink = new SinkExpander(env);
        BoundExpander bound = new BoundExpander(env);

        for (MoveKind move : MoveKind.values()) {
            expanders[move.ordinal()] = switch (move) {
                case DROP -> sink;
                case RAISE -> raise;
                case STEP_NORTH, STEP_SOUTH, STEP_EAST, STEP_WEST -> step;
                case STEP_UP_NORTH, STEP_UP_SOUTH, STEP_UP_EAST, STEP_UP_WEST -> stepUp;
                case DROP_EAST, DROP_WEST, DROP_NORTH, DROP_SOUTH -> drop;
                case LEAN_NORTHEAST, LEAN_NORTHWEST, LEAN_SOUTHEAST, LEAN_SOUTHWEST -> lean;
                case BOUND_NORTH, BOUND_SOUTH, BOUND_EAST, BOUND_WEST -> bound;
            };
        }
        return expanders;
    }
}