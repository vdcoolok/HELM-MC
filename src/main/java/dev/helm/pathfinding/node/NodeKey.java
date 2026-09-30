package dev.helm.pathfinding.node;

public final class NodeKey {

    private static final int COORD_BITS = 26;
    private static final int LEVEL_BITS = 12;

    private static final int LEVEL_SHIFT = COORD_BITS;
    private static final int COORD_SHIFT = LEVEL_BITS + COORD_BITS;

    private static final int COORD_LIMIT = 1 << (COORD_BITS - 1);
    private static final int LEVEL_LIMIT = 1 << (LEVEL_BITS - 1);

    private static final long COORD_MASK = (1L << COORD_BITS) - 1;
    private static final long LEVEL_MASK = (1L << LEVEL_BITS) - 1;

    private NodeKey() {
    }

    public static long of(int x, int y, int z) {
        if (!fits(x) || !fits(z) || y < -LEVEL_LIMIT || y >= LEVEL_LIMIT) {
            throw new IllegalArgumentException(
                    "Position outside packable range: " + x + " " + y + " " + z);
        }
        return ((long) x << COORD_SHIFT)
                | ((long) (y & LEVEL_MASK) << LEVEL_SHIFT)
                | (z & COORD_MASK);
    }

    public static int x(long key) {
        return (int) (key >> COORD_SHIFT);
    }

    public static int y(long key) {
        return (int) ((key << LEVEL_SHIFT) >> (64 - LEVEL_BITS));
    }

    public static int z(long key) {
        return (int) ((key << COORD_SHIFT) >> COORD_SHIFT);
    }

    public static boolean fits(int value) {
        return value >= -COORD_LIMIT && value < COORD_LIMIT;
    }
}