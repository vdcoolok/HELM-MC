package dev.helm.pathfinding.move;

public final class MoveTarget {

    private static final double IMPOSSIBLE = dev.helm.pathfinding.cost.MoveCosts.IMPOSSIBLE;

    public int x;
    public int y;
    public int z;
    public double cost;

    public void reset(int fromX, int fromY, int fromZ, MoveKind move) {
        this.x = fromX + move.offsetX;
        this.y = fromY + move.offsetY;
        this.z = fromZ + move.offsetZ;
        this.cost = IMPOSSIBLE;
    }
}
