package dev.helm.pathfinding.goal;

public final class BlockGoal implements Goal {

    private final int x;
    private final int y;
    private final int z;

    public BlockGoal(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
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

    @Override
    public double estimate(int otherX, int otherY, int otherZ) {
        double dx = (double) otherX - x;
        double dy = (double) otherY - y;
        double dz = (double) otherZ - z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    @Override
    public boolean reached(int otherX, int otherY, int otherZ) {
        return otherX == x && otherY == y && otherZ == z;
    }
}