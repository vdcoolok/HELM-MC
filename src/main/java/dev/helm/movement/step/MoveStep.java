package dev.helm.movement.step;

import java.util.List;

public interface MoveStep {

    int fromX();

    int fromY();

    int fromZ();

    int toX();

    int toY();

    int toZ();

    List<int[]> blocksToBreak();

    int[] placeAt();

    List<int[]> blocksToWalkInto();

    default int directionX() {
        return Integer.signum(toX() - fromX());
    }

    default int directionY() {
        return toY() - fromY();
    }

    default int directionZ() {
        return Integer.signum(toZ() - fromZ());
    }
}
