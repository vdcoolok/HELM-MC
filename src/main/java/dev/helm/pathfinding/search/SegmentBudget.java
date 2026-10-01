package dev.helm.pathfinding.search;

import dev.helm.setting.PathSettings;

public record SegmentBudget(int primaryMillis, int failureMillis) {

    public static SegmentBudget first(PathSettings path) {
        return new SegmentBudget(path.primaryTimeoutMillis(), path.failureTimeoutMillis());
    }

    public static SegmentBudget beyond(PathSettings path) {
        return new SegmentBudget(path.planAheadPrimaryTimeoutMillis(),
                path.planAheadFailureTimeoutMillis());
    }

    public SearchBudget with(PathSettings path) {
        return new SearchBudget(primaryMillis, failureMillis, path.maxChunkBorderFetch(),
                path.repropagateImprovement() ? SearchBudget.MIN_IMPROVEMENT : 0);
    }
}
