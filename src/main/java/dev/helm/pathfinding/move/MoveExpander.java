package dev.helm.pathfinding.move;

public interface MoveExpander {

    MoveTarget expand(int fromX, int fromY, int fromZ, MoveTarget target);
}