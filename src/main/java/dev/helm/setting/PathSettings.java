package dev.helm.setting;

public final class PathSettings extends SettingSection {

    public static final String RENDER_PATH = "path.renderPath";
    public static final String RENDER_AS_LINE = "path.renderPathAsLine";
    public static final String RENDER_GOAL = "path.renderGoal";
    public static final String FADE_PATH = "path.fadePath";
    public static final String RENDER_BREAKS = "path.renderBlocksToBreak";
    public static final String RENDER_PLACES = "path.renderBlocksToPlace";
    public static final String LINE_WIDTH = "path.lineWidth";
    public static final String GOAL_LINE_WIDTH = "path.goalLineWidth";
    public static final String ENABLED = "path.enable";
    public static final String PRIMARY_TIMEOUT = "path.primaryTimeoutMillis";
    public static final String FAILURE_TIMEOUT = "path.failureTimeoutMillis";
    public static final String CHUNK_BORDER_FETCH = "path.maxChunkBorderFetch";
    public static final String REPROPAGATE_IMPROVEMENT = "path.repropagateImprovement";
    public static final String CUTOFF_AT_LOAD_BOUNDARY = "path.cutoffAtLoadBoundary";
    public static final String CUTOFF_MINIMUM_LENGTH = "path.cutoffMinimumLength";
    public static final String CUTOFF_FACTOR = "path.cutoffFactor";

    public PathSettings() {
        flag(RENDER_PATH, "Render path", "Draw the path being followed.", true);
        flag(RENDER_AS_LINE, "Render path as a line",
                "Draw a straight line through the path instead of boxes.", false);
        flag(RENDER_GOAL, "Render goal", "Draw the goal.", true);
        flag(FADE_PATH, "Fade path", "Path fades out with distance.", false);
        flag(RENDER_BREAKS, "Render blocks to break",
                "Outline blocks that will be mined.", true);
        flag(RENDER_PLACES, "Render blocks to place",
                "Outline blocks that will be placed.", true);
        amount(LINE_WIDTH, "Path line width",
                "Thickness of the path line, in pixels.", 5.0D, 1.0D, 32.0D);
        amount(GOAL_LINE_WIDTH, "Goal line width",
                "Thickness of the goal line, in pixels.", 3.0D, 1.0D, 32.0D);
        flag(ENABLED, "Pathfinding enabled",
                "Whether navigation is allowed to run at all.", true);
        count(PRIMARY_TIMEOUT, "Primary timeout",
                "Milliseconds allowed before the search has moved away from the start.",
                500, 0, 600000);
        count(FAILURE_TIMEOUT, "Failure timeout",
                "Milliseconds allowed before the search gives up entirely.", 2000, 0, 600000);
        count(CHUNK_BORDER_FETCH, "Chunk border fetch limit",
                "How many moves into unloaded chunks the search may consider.", 50, 0, 10000);
        flag(REPROPAGATE_IMPROVEMENT, "Repropagate improvements",
                "Require a minimum cost improvement before a node is revisited.", true);
        flag(CUTOFF_AT_LOAD_BOUNDARY, "Cut off at the loaded boundary",
                "Drop the tail of a path that runs into chunks the game has not loaded.", false);
        count(CUTOFF_MINIMUM_LENGTH, "Cut off minimum length",
                "Paths shorter than this are never shortened.", 30, 0, 10000);
        amount(CUTOFF_FACTOR, "Cut off factor",
                "How much of a long unfinished path is kept.", 0.9D, 0.0D, 1.0D);
    }

    public boolean renderPath() {
        return on(RENDER_PATH);
    }

    public boolean renderPathAsLine() {
        return on(RENDER_AS_LINE);
    }

    public boolean renderGoal() {
        return on(RENDER_GOAL);
    }

    public boolean fadePath() {
        return on(FADE_PATH);
    }

    public boolean renderBlocksToBreak() {
        return on(RENDER_BREAKS);
    }

    public boolean renderBlocksToPlace() {
        return on(RENDER_PLACES);
    }

    public double lineWidth() {
        return rate(LINE_WIDTH);
    }

    public double goalLineWidth() {
        return rate(GOAL_LINE_WIDTH);
    }

    public boolean enabled() {
        return on(ENABLED);
    }

    public int primaryTimeoutMillis() {
        return level(PRIMARY_TIMEOUT);
    }

    public int failureTimeoutMillis() {
        return level(FAILURE_TIMEOUT);
    }

    public int maxChunkBorderFetch() {
        return level(CHUNK_BORDER_FETCH);
    }

    public boolean repropagateImprovement() {
        return on(REPROPAGATE_IMPROVEMENT);
    }

    public boolean cutoffAtLoadBoundary() {
        return on(CUTOFF_AT_LOAD_BOUNDARY);
    }

    public int cutoffMinimumLength() {
        return level(CUTOFF_MINIMUM_LENGTH);
    }

    public double cutoffFactor() {
        return rate(CUTOFF_FACTOR);
    }
}
