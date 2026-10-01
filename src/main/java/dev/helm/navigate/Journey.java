package dev.helm.navigate;

import dev.helm.diag.Trace;
import dev.helm.movement.Route;
import dev.helm.pathfinding.path.NodePath;
import dev.helm.pathfinding.path.PathCutoff;
import dev.helm.pathfinding.search.Search;
import dev.helm.pathfinding.search.SearchOutcome;
import dev.helm.pathfinding.world.BlockView;
import dev.helm.pathfinding.world.block.WalkRules;
import dev.helm.setting.PathSettings;
import dev.helm.setting.Settings;

public final class Journey {

    public record Result(SearchOutcome outcome, Route route, int visited) {

        public boolean reached() {
            return outcome == SearchOutcome.REACHED_GOAL;
        }

        public boolean arrived() {
            return reached() && route.steps().isEmpty();
        }

        public boolean usable() {
            return outcome.usable() && !route.steps().isEmpty();
        }
    }

    private Journey() {
    }

    public static Result collect(Search search, BlockView world, WalkRules walk) {
        SearchOutcome outcome = search.outcome();
        NodePath found = search.partial();
        Route route = RouteMaker.from(world, walk, shorten(found, world, outcome));
        Trace.instance().event("search", "outcome " + outcome
                + " stopped: " + search.stopReason()
                + " visited " + search.visited()
                + " stored " + search.stored()
                + " steps " + route.length());
        if (found == null) {
            Trace.instance().event("search", "no partial path: best never moved far enough "
                    + "from the start");
        }
        return new Result(outcome, route, search.visited());
    }

    private static NodePath shorten(NodePath found, BlockView world, SearchOutcome outcome) {
        if (found == null) {
            return null;
        }
        PathSettings path = Settings.holder().path();
        int before = found.length();
        NodePath kept = PathCutoff.apply(found, world, path.cutoffAtLoadBoundary());
        if (outcome != SearchOutcome.REACHED_GOAL) {
            kept = PathCutoff.shortOfGoal(kept, path.cutoffMinimumLength(),
                    path.cutoffFactor());
        }
        if (kept.length() != before) {
            Trace.instance().event("search", "shortened a partial path from "
                    + before + " to " + kept.length() + " steps");
        }
        return kept;
    }
}
