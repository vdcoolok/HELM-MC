package dev.helm.pathfinding.search;

import java.util.Arrays;

import dev.helm.pathfinding.node.NodeKey;

public final class RouteFavor {

    private static final double PLAIN = 1.0D;
    private static final RouteFavor NONE = new RouteFavor(new long[0], PLAIN);

    private final long[] cheapened;
    private final double multiplier;

    private RouteFavor(long[] cheapened, double multiplier) {
        this.cheapened = cheapened;
        this.multiplier = multiplier;
    }

    public static RouteFavor none() {
        return NONE;
    }

    public static RouteFavor along(int[][] blocks, double multiplier) {
        if (blocks == null || blocks.length == 0 || multiplier == PLAIN) {
            return NONE;
        }
        long[] cheapened = new long[blocks.length];
        for (int index = 0; index < blocks.length; index++) {
            cheapened[index] = NodeKey.of(blocks[index][0], blocks[index][1], blocks[index][2]);
        }
        Arrays.sort(cheapened);
        return new RouteFavor(cheapened, multiplier);
    }

    public boolean active() {
        return cheapened.length > 0;
    }

    public double multiplierFor(int x, int y, int z) {
        if (cheapened.length == 0) {
            return PLAIN;
        }
        return Arrays.binarySearch(cheapened, NodeKey.of(x, y, z)) >= 0 ? multiplier : PLAIN;
    }
}
