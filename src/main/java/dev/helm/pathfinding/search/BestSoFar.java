package dev.helm.pathfinding.search;

import dev.helm.pathfinding.node.Node;

public final class BestSoFar {

    private final double[] bestEstimate = new double[SearchBudget.COEFFICIENTS.length];
    private final Node[] best = new Node[SearchBudget.COEFFICIENTS.length];

    public void reset() {
        java.util.Arrays.fill(best, null);
    }

    public void seed(Node start) {
        for (int index = 0; index < best.length; index++) {
            bestEstimate[index] = start.estimate;
            best[index] = start;
        }
    }

    public boolean offer(Node node, double minImprovement) {
        boolean improved = false;
        for (int index = 0; index < best.length; index++) {
            double score = node.estimate + node.cost / SearchBudget.COEFFICIENTS[index];
            if (bestEstimate[index] - score > minImprovement) {
                bestEstimate[index] = score;
                best[index] = node;
                improved = true;
            }
        }
        return improved;
    }

    public Node best(int startX, int startY, int startZ) {
        for (int index = 0; index < best.length; index++) {
            Node candidate = best[index];
            if (candidate == null) {
                continue;
            }
            if (candidate.distanceFromSq(startX, startY, startZ)
                    > SearchBudget.MIN_PROGRESS * SearchBudget.MIN_PROGRESS) {
                return candidate;
            }
        }
        return null;
    }

    public double furthestDistance(int startX, int startY, int startZ) {
        double furthest = 0;
        for (Node candidate : best) {
            if (candidate == null) {
                continue;
            }
            furthest = Math.max(furthest, candidate.distanceFromSq(startX, startY, startZ));
        }
        return furthest;
    }
}