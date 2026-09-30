package dev.helm.pathfinding.move;

public final class MoveTarget {

    public int x;
    public int y;
    public int z;
    public double cost;

    public void placeAt(int fromX, int fromY, int fromZ, MoveKind move) {
        this.x = fromX + move.offsetX;
        this.y = fromY + move.offsetY;
        this.z = fromZ + move.offsetZ;
    }

    public void placeAt(int x, int y, int z, double cost) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.cost = cost;
    }

    public void clear() {
        this.cost = dev.helm.pathfinding.cost.MoveCosts.IMPOSSIBLE;
    }
}