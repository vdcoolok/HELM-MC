package dev.helm.navigate;

import dev.helm.movement.Route;
import dev.helm.pathfinding.goal.Goal;
import dev.helm.pathfinding.search.RouteFavor;
import dev.helm.pathfinding.search.SearchJob;
import dev.helm.pathfinding.search.SegmentBudget;
import dev.helm.setting.Settings;

public final class SegmentPlanner {

    private SegmentPlanner() {
    }

    public static SearchJob first(Navigator navigator, Goal goal, int x, int y, int z) {
        return navigator.searchFromHere(goal, x, y, z,
                SegmentBudget.first(Settings.holder().path()), RouteFavor.none());
    }

    public static SearchJob beyond(Navigator navigator, Goal goal, int x, int y, int z,
                                   RouteFavor favor) {
        return navigator.searchFromHere(goal, x, y, z,
                SegmentBudget.beyond(Settings.holder().path()), favor);
    }

    public static RouteFavor favorOf(Route segment) {
        return RouteFavor.along(segment.positions(),
                Settings.holder().path().backtrackCostFavoringCoefficient());
    }

    public static boolean dueFor(double ticksLeftInCurrentSegment) {
        return ticksLeftInCurrentSegment < Settings.holder().path().planningTickLookahead();
    }
}
