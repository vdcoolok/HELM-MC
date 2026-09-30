package dev.helm.pathfinding.cost;

public final class MoveCosts {

    public static final double WALK_ONE = 20 / 4.317;
    public static final double WALK_ONE_IN_WATER = 20 / 2.2;
    public static final double WALK_ONE_OVER_SOUL_SAND = WALK_ONE * 2;
    public static final double LADDER_UP_ONE = 20 / 2.35;
    public static final double LADDER_DOWN_ONE = 20 / 3.0;
    public static final double SNEAK_ONE = 20 / 1.3;
    public static final double SPRINT_ONE = 20 / 5.612;
    public static final double SPRINT_MULTIPLIER = SPRINT_ONE / WALK_ONE;
    public static final double WALK_OFF_BLOCK = WALK_ONE * 0.8;
    public static final double CENTER_AFTER_FALL = WALK_ONE - WALK_OFF_BLOCK;

    public static final double IMPOSSIBLE = 1_000_000;

    private MoveCosts() {
    }
}