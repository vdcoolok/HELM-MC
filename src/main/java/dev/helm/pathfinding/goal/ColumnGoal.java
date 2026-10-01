package dev.helm.pathfinding.goal;

public final class ColumnGoal implements Goal {

    private final int x;
    private final int z;

    public ColumnGoal(int x, int z) {
        this.x = x;
        this.z = z;
    }

    public int x() {
        return x;
    }

    public int z() {
        return z;
    }

    @Override
    public double estimate(int otherX, int otherY, int otherZ) {
        return GoalDistances.flat(otherX - x, otherZ - z);
    }

    @Override
    public boolean reached(int otherX, int otherY, int otherZ) {
        return otherX == x && otherZ == z;
    }

    @Override
    public String toString() {
        return x + " " + z;
    }
}
