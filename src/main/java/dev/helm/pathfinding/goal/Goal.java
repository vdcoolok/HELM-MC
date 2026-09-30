package dev.helm.pathfinding.goal;

public interface Goal {

    double estimate(int x, int y, int z);

    boolean reached(int x, int y, int z);
}