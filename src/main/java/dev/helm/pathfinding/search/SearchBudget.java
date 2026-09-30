package dev.helm.pathfinding.search;

public final class SearchBudget {

    public static final double[] COEFFICIENTS = {1.5, 2, 2.5, 3, 4, 5, 10};

    public static final double MIN_PROGRESS = 5;
    public static final double MIN_IMPROVEMENT = 0.01;

    private static final int CLOCK_MASK = (1 << 6) - 1;

    private final long primaryMillis;
    private final long failureMillis;
    private final int maxUnloadedCrossings;
    private final double minImprovement;

    private long primaryDeadline;
    private long failureDeadline;
    private int unloadedCrossings;
    private boolean reached;
    private volatile boolean cancelled;

    public SearchBudget(long primaryMillis, long failureMillis, int maxUnloadedCrossings,
                        double minImprovement) {
        this.primaryMillis = primaryMillis;
        this.failureMillis = failureMillis;
        this.maxUnloadedCrossings = maxUnloadedCrossings;
        this.minImprovement = minImprovement;
    }

    public void begin(long now) {
        primaryDeadline = now + primaryMillis;
        failureDeadline = now + failureMillis;
        unloadedCrossings = 0;
        reached = false;
        cancelled = false;
    }

    public void cancel() {
        cancelled = true;
    }

    public boolean cancelled() {
        return cancelled;
    }

    public double improvement() {
        return minImprovement;
    }

    public int coefficients() {
        return COEFFICIENTS.length;
    }

    public void countUnloadedCrossing() {
        unloadedCrossings++;
    }

    public void noteProgress(double distanceFromStartSq) {
        if (!reached && distanceFromStartSq > MIN_PROGRESS * MIN_PROGRESS) {
            reached = true;
        }
    }

    public boolean spent(int visited, long now) {
        if (unloadedCrossings >= maxUnloadedCrossings) {
            return true;
        }
        if ((visited & CLOCK_MASK) != 0) {
            return false;
        }
        return now - failureDeadline >= 0 || (!reached && now - primaryDeadline >= 0);
    }
}