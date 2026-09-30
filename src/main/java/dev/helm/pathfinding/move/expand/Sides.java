package dev.helm.pathfinding.move.expand;

public final class Sides {

    public static final int[][] AGAINST_ALL_BUT_UP = {
            {0, 0, -1},
            {0, 0, +1},
            {+1, 0, 0},
            {-1, 0, 0},
            {0, -1, 0}
    };

    public static final int[][] FLAT = {
            {0, 0, -1},
            {0, 0, +1},
            {+1, 0, 0},
            {-1, 0, 0}
    };

    private Sides() {
    }

    static {
        if (AGAINST_ALL_BUT_UP.length != 5 || FLAT.length != 4) {
            throw new IllegalStateException("Side tables are malformed");
        }
    }
}