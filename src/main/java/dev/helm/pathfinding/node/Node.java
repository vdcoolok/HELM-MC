package dev.helm.pathfinding.node;

import dev.helm.pathfinding.cost.MoveCosts;
import dev.helm.pathfinding.goal.Goal;
import dev.helm.pathfinding.move.MoveKind;

public final class Node {

    private static final int NOT_QUEUED = -1;

    public final int x;
    public final int y;
    public final int z;

    public final double estimate;

    public double cost;
    public double combined;
    public Node previous;
    public int queuedAt;
    public MoveKind arrivedBy;

    public Node(int x, int y, int z, Goal goal) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.cost = MoveCosts.IMPOSSIBLE;
        this.queuedAt = NOT_QUEUED;
        this.estimate = goal.estimate(x, y, z);
        if (Double.isNaN(this.estimate)) {
            throw new IllegalStateException("Goal produced an unusable estimate at " + x + " " + y + " " + z);
        }
    }

    public boolean queued() {
        return queuedAt != NOT_QUEUED;
    }

    public void adopt(Node from, double reachCost, MoveKind by) {
        this.previous = from;
        this.arrivedBy = by;
        this.cost = from.cost + reachCost;
        this.combined = this.cost + this.estimate;
    }

    public double distanceFromSq(int startX, int startY, int startZ) {
        long dx = (long) x - startX;
        long dy = (long) y - startY;
        long dz = (long) z - startZ;
        return dx * dx + dy * dy + dz * dz;
    }
}
