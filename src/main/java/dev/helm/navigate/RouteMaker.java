package dev.helm.navigate;

import java.util.ArrayList;
import java.util.List;

import dev.helm.movement.Route;
import dev.helm.pathfinding.move.MoveKind;
import dev.helm.pathfinding.path.NodePath;
import dev.helm.movement.step.PlanStep;
import dev.helm.movement.step.StepBuilder;
import dev.helm.pathfinding.world.BlockView;
import dev.helm.pathfinding.world.block.WalkRules;

public final class RouteMaker {

    private RouteMaker() {
    }

    public static Route from(BlockView world, WalkRules walk, NodePath path) {
        if (path == null || path.isEmpty()) {
            return Route.empty();
        }
        List<PlanStep> steps = new ArrayList<>(path.length());
        for (NodePath.Leg leg : path.legs()) {
            steps.add(StepBuilder.build(world, walk, leg));
        }
        return Route.of(steps, path.end());
    }

    public static MoveKind kindOf(NodePath.Leg leg) {
        return leg.by();
    }
}
