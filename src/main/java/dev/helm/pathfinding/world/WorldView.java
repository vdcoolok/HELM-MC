package dev.helm.pathfinding.world;

public interface WorldView {

    boolean loaded(int x, int z);

    boolean residentChunk(int x, int z);

    int lowestLevel();

    int levelCount();

    boolean entirelyInsideBorder(int x, int z);

    boolean canPlaceAt(int x, int z);

    default int ceilingLevel() {
        return lowestLevel() + levelCount();
    }

    default boolean insideBuildHeight(int y) {
        return y >= lowestLevel() && y < ceilingLevel();
    }
}
