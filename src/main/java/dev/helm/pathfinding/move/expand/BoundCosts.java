package dev.helm.pathfinding.move.expand;

import dev.helm.pathfinding.cost.MoveCosts;

public final class BoundCosts {

    private BoundCosts() {
    }

    public static double forJumpDistance(int distance) {
        return switch (distance) {
            case 2 -> MoveCosts.WALK_ONE * 2;
            case 3 -> MoveCosts.WALK_ONE * 3;
            case 4 -> MoveCosts.SPRINT_ONE * 4;
            default -> throw new IllegalStateException("Unreachable gap span " + distance);
        };
    }
}