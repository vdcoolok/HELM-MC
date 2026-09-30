package dev.helm.pathfinding.move;

public enum MoveKind {

    DROP(0, -1, 0, false, false),
    RAISE(0, +1, 0, false, false),
    STEP_NORTH(0, 0, -1, false, false),
    STEP_SOUTH(0, 0, +1, false, false),
    STEP_EAST(+1, 0, 0, false, false),
    STEP_WEST(-1, 0, 0, false, false),
    STEP_UP_NORTH(0, +1, -1, false, false),
    STEP_UP_SOUTH(0, +1, +1, false, false),
    STEP_UP_EAST(+1, +1, 0, false, false),
    STEP_UP_WEST(-1, +1, 0, false, false),
    DROP_EAST(+1, -1, 0, true, true),
    DROP_WEST(-1, -1, 0, true, true),
    DROP_NORTH(0, -1, -1, true, true),
    DROP_SOUTH(0, -1, +1, true, true),
    LEAN_NORTHEAST(+1, 0, -1, true, true),
    LEAN_NORTHWEST(-1, 0, -1, true, true),
    LEAN_SOUTHEAST(+1, 0, +1, true, true),
    LEAN_SOUTHWEST(-1, 0, +1, true, true),
    BOUND_NORTH(0, 0, -4, true, true),
    BOUND_SOUTH(0, 0, +4, true, true),
    BOUND_EAST(+4, 0, 0, true, true),
    BOUND_WEST(-4, 0, 0, true, true);

    private static final MoveKind[] ORDER = values();

    public final int offsetX;
    public final int offsetY;
    public final int offsetZ;
    public final boolean offsetFollowsBlock;
    public final boolean levelFollowsBlock;

    MoveKind(int offsetX, int offsetY, int offsetZ, boolean offsetFollowsBlock,
             boolean levelFollowsBlock) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.offsetZ = offsetZ;
        this.offsetFollowsBlock = offsetFollowsBlock;
        this.levelFollowsBlock = levelFollowsBlock;
    }

    public static MoveKind[] order() {
        return ORDER;
    }

    public boolean fixedX() {
        return !offsetFollowsBlock;
    }

    public boolean fixedLevel() {
        return !levelFollowsBlock;
    }
}