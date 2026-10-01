package dev.helm.movement;

import dev.helm.movement.step.PlanStep;

public final class RouteResync {

    private static final int LOOKAHEAD = 3;

    private RouteResync() {
    }

    public static int earlier(Route route, int index, int x, int y, int z) {
        for (int candidate = 0; candidate < index; candidate++) {
            if (holds(route.at(candidate), x, y, z)) {
                return candidate;
            }
        }
        return -1;
    }

    public static int later(Route route, int index, int x, int y, int z) {
        for (int candidate = index + LOOKAHEAD; candidate < route.length() - 1; candidate++) {
            if (holds(route.at(candidate), x, y, z)) {
                return candidate;
            }
        }
        return -1;
    }

    private static boolean holds(PlanStep step, int x, int y, int z) {
        return step.footprint().contains(x, y, z);
    }
}
