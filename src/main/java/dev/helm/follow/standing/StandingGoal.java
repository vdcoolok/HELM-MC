package dev.helm.follow.standing;

import java.util.ArrayList;
import java.util.List;

import dev.helm.pathfinding.goal.AnyGoal;
import dev.helm.pathfinding.goal.Goal;
import dev.helm.pathfinding.goal.NearGoal;
import dev.helm.setting.FollowSettings;

public record StandingGoal(Target target, StandingSpot spot, Goal goal) {

    public static StandingGoal towards(List<Target> targets, FollowSettings settings) {
        Target closest = TargetWatch.closest(targets);
        List<Goal> spots = new ArrayList<>(targets.size());
        for (Target target : targets) {
            spots.add(goalOn(target, settings));
        }
        Goal goal = spots.size() == 1 ? spots.getFirst() : AnyGoal.of(spots);
        return new StandingGoal(closest, spotOn(closest, settings), goal);
    }

    public boolean reached(int feetX, int feetY, int feetZ) {
        return goal.reached(feetX, feetY, feetZ);
    }

    private static NearGoal goalOn(Target target, FollowSettings settings) {
        StandingSpot where = spotOn(target, settings);
        return new NearGoal(where.x(), where.y(), where.z(), settings.radius(),
                settings.verticalRadius());
    }

    private static StandingSpot spotOn(Target target, FollowSettings settings) {
        return OffsetBearing.behind(target, settings.offsetDirection(),
                settings.offsetDistance(), settings.verticalOffset());
    }
}