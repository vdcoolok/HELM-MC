package dev.helm.pathfinding.search;

public enum SearchOutcome {

    REACHED_GOAL,
    PARTIAL_PATH,
    NO_PATH,
    CANCELLED,
    FAILED;

    public boolean usable() {
        return this == REACHED_GOAL || this == PARTIAL_PATH;
    }
}