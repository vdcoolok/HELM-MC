package dev.helm.pathfinding.goal;

public final class BesideGoal implements Goal {

    protected final int x;
    protected final int y;
    protected final int z;

    public BesideGoal(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public boolean reached(int otherX, int otherY, int otherZ) {
        int xDiff = otherX - x;
        int yDiff = otherY - y;
        int zDiff = otherZ - z;
        return Math.abs(xDiff) + Math.abs(droppingInto(yDiff)) + Math.abs(zDiff) <= 1;
    }

    @Override
    public double estimate(int otherX, int otherY, int otherZ) {
        int yDiff = otherY - y;
        return GoalDistances.level(0, droppingInto(yDiff))
                + GoalDistances.flat(otherX - x, otherZ - z);
    }

    protected static int droppingInto(int yDiff) {
        return yDiff < 0 ? yDiff + 1 : yDiff;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        BesideGoal that = (BesideGoal) other;
        return x == that.x && y == that.y && z == that.z;
    }

    @Override
    public int hashCode() {
        return x * 31 * 31 + y * 31 + z;
    }

    @Override
    public String toString() {
        return "beside " + x + " " + y + " " + z;
    }
}