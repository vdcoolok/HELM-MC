package dev.helm.navigate;

import dev.helm.movement.Route;
import dev.helm.pathfinding.search.Search;
import dev.helm.pathfinding.search.SearchOutcome;
import dev.helm.pathfinding.world.BlockView;
import dev.helm.pathfinding.world.block.WalkRules;

public final class Journey {

    public record Result(SearchOutcome outcome, Route route, int visited) {

        public boolean reached() {
            return outcome == SearchOutcome.REACHED_GOAL;
        }

        public boolean usable() {
            return outcome.usable() && !route.steps().isEmpty();
        }
    }

    private Journey() {
    }

    public static Result collect(Search search, BlockView world, WalkRules walk) {
        return new Result(search.outcome(), RouteMaker.from(world, walk, search.partial()),
                search.visited());
    }
}
