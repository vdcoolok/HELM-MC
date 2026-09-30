package dev.helm.pathfinding.search;

public final class SearchBudget {

    public static final double[] COEFFICIENTS = {1.5, 2, 2.5, 3, 4, 5, 10};
    public static final double MIN_PROGRESS = 5;
    public static final double MIN_IMPROVEMENT = 0.01;

    private static final int CLOCK_INTERVAL = 1 << 6;
    private static final int CLOCK_MASK = CLOCK_INTERVAL - 1;

    private final long primaryMillis;
    private final long failureMillis;
    private final int maxChunkBorderFetch;
    private final double minImprovement;

    private long primaryDeadline;
    private long failureDeadline;
    private int chunkBorderFetches;
    private boolean madeProgress;
    private boolean cancelled;

    public SearchBudget(long primaryMillis, long failureMillis, int maxChunkBorderFetch,
                        double minImprovement) {
        this.primaryMillis = primaryMillis;
        this.failureMillis = failureMillis;
        this.maxChunkBorderFetch = maxChunkBorderFetch;
        this.minImprovement = minImprovement;
    }

    public void begin(long nowMillis) {
        this.primaryDeadline = nowMillis + primaryMillis;
        this.failureDeadline = nowMillis + failureMillis;
        this.chunkBorderFetches = 0;
        this.madeProgress = false;
        this.cancelled = false;
    }

    public void cancel() {
        this.cancelled = true;
    }

    public boolean cancelled() {
        return cancelled;
    }

    public double improvement() {
        return minImprovement;
    }

    public void countUnloadedCrossing() {
        chunkBorderFetches++;
    }

    public boolean mayCrossUnloadedChunks() {
        return chunkBorderFetches < maxChunkBorderFetch;
    }

    public void noteProgress(double distanceFromStartSq) {
        if (!madeProgress && distanceFromStartSq > MIN_PROGRESS * MIN_PROGRESS) {
            madeProgress = true;
        }
    }

    public boolean dueForClockCheck(int visited) {
        return (visited & CLOCK_MASK) == 0;
    }

    public boolean exhausted(long nowMillis) {
        return nowMillis - failureDeadline >= 0
                || (!madeProgress && nowMillis - primaryDeadline >= 0);
    }

    public String whySpent(long nowMillis) {
        if (!mayCrossUnloadedChunks()) {
            return "chunk border fetch limit of " + maxChunkBorderFetch + " reached";
        }
        if (nowMillis - failureDeadline >= 0) {
            return "failure timeout of " + failureMillis + "ms";
        }
        if (!madeProgress && nowMillis - primaryDeadline >= 0) {
            return "primary timeout of " + primaryMillis + "ms without progress";
        }
        return "open set emptied";
    }
}
