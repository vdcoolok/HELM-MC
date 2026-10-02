package dev.helm.pathfinding.goal;

public final class BreakableGoal implements Goal {

    private final BesideGoal beside;

    public BreakableGoal(int x, int y, int z) {
        this.beside = new BesideGoal(x, y, z);
    }

    @Override
    public boolean reached(int otherX, int otherY, int otherZ) {
        return otherY <= beside.y && beside.reached(otherX, otherY, otherZ);
    }

    @Override
    public double estimate(int otherX, int otherY, int otherZ) {
        return beside.estimate(otherX, otherY, otherZ);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof BreakableGoal that && beside.equals(that.beside);
    }

    @Override
    public int hashCode() {
        return beside.hashCode() * 31;
    }

    @Override
    public String toString() {
        return "in reach of " + beside;
    }
}