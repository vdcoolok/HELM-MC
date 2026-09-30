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
}
