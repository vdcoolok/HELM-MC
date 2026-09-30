package dev.helm.pathfinding.world;

public interface WorldView {

    boolean loaded(int x, int z);

    int lowestLevel();

    int levelCount();

    boolean insideBorder(int x, int z);
}