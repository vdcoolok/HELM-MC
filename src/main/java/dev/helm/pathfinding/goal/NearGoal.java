package dev.helm.pathfinding.goal;

public final class NearGoal implements Goal {

    private final int x;
    private final int y;
    private final int z;
    private final int flatSquared;
    private final int upSquared;

    public NearGoal(int x, int y, int z, int flatRange, int upRange) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.flatSquared = squared(flatRange);
        this.upSquared = squared(upRange);
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public int z() {
        return z;
    }

    public int flatRange() {
        return (int) Math.sqrt(flatSquared);
    }

    public int upRange() {
        return (int) Math.sqrt(upSquared);
    }

    @Override
    public boolean reached(int otherX, int otherY, int otherZ) {
        int across = otherX - x;
        int along = otherZ - z;
        if (across * across + along * along > flatSquared) {
            return false;
        }
        int rise = otherY - y;
        return rise * rise <= upSquared;
    }

    @Override
    public double estimate(int otherX, int otherY, int otherZ) {
        return GoalDistances.level(y, otherY) + GoalDistances.flat(otherX - x, otherZ - z);
    }

    private static int squared(int range) {
        int span = Math.max(0, range);
        return span * span;
    }

    @Override
    public String toString() {
        return "within " + flatRange() + " across and " + upRange() + " up of " + x + " " + y
                + " " + z;
    }
}