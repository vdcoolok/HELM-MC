package dev.helm.setting;

public final class FollowSettings extends SettingSection {

    public static final String RADIUS = "follow.radius";
    public static final String VERTICAL_RADIUS = "follow.verticalRadius";
    public static final String OFFSET_DISTANCE = "follow.offsetDistance";
    public static final String OFFSET_DIRECTION = "follow.offsetDirection";
    public static final String VERTICAL_OFFSET = "follow.verticalOffset";
    public static final String MAX_TARGET_DISTANCE = "follow.maxTargetDistance";
    public static final String WAIT_TICKS = "follow.waitTicks";
    public static final String RETRY_TICKS = "follow.retryTicks";
    public static final String REPLAN_TICKS = "follow.replanTicks";
    public static final String MIN_TARGET_DISTANCE = "follow.minTargetDistance";
    public static final String HOLD_WHEN_CLOSE = "follow.holdWhenClose";
    public static final String SPRINT = "follow.sprint";
    public static final String BREAK_BLOCKS = "follow.breakBlocks";
    public static final String PLACE_BLOCKS = "follow.placeBlocks";
    public static final String KEEP_TARGET = "follow.keepTarget";
    public static final String CLOSEST_ONLY = "follow.closestOnly";
    public static final String IGNORE_SAME_KIND = "follow.ignoreSameKind";
    public static final String LOOK_AT_TARGET = "follow.lookAtTarget";
    public static final String MAX_LOOK_PITCH = "follow.maxLookPitch";

    public FollowSettings() {
        count(RADIUS, "Follow radius",
                "How many blocks across from the spot count as arrived. Zero means HELM "
                        + "has to stand on the exact column.", 3, 0, 32);
        count(VERTICAL_RADIUS, "Follow vertical radius",
                "How many blocks up or down from the spot count as arrived. Zero means "
                        + "only the target's own level.", 2, 0, 32);
        amount(OFFSET_DISTANCE, "Follow offset distance",
                "Stand this many blocks away from the target instead of on it. Zero walks "
                        + "onto the target's own block.", 0.0D, 0.0D, 64.0D);
        amount(OFFSET_DIRECTION, "Follow offset direction",
                "Which side of the target the offset sits on, in degrees. Zero is south, "
                        + "ninety is west.", 0.0D, -360.0D, 360.0D);
        amount(VERTICAL_OFFSET, "Follow vertical offset",
                "How many blocks up or down from the target the spot sits. Negative is "
                        + "below, so a negative value stands under a flying target.", 0.0D,
                -32.0D, 32.0D);
        count(MAX_TARGET_DISTANCE, "Follow target max distance",
                "Ignore anything further away than this many blocks. Zero has no limit.", 0, 0, 512);
        count(MIN_TARGET_DISTANCE, "Follow target min distance",
                "Ignore anything nearer than this many blocks, so a mob that has walked "
                        + "into you is no longer a target. Zero has no minimum.", 0, 0, 64);
        count(WAIT_TICKS, "Follow wait ticks",
                "How long to keep waiting when nothing matches, in case the target has only "
                        + "stepped out of sight. Zero gives up at once.", 20, 0, 600);
        count(RETRY_TICKS, "Follow retry ticks",
                "How long to wait before searching again after no path could be found.", 20, 0, 600);
        count(REPLAN_TICKS, "Follow replan ticks",
                "How long to wait after the target moves before searching again. Zero "
                        + "reacts on the very next tick.", 2, 0, 200);
        flag(HOLD_WHEN_CLOSE, "Hold when close",
                "Stand still once arrived instead of taking another step towards a target that "
                        + "is already within the radius.", true);
        flag(SPRINT, "Follow sprint",
                "Allow sprinting on the way to the target.", true);
        flag(BREAK_BLOCKS, "Follow break blocks",
                "Allow breaking blocks that stand between you and the target.", true);
        flag(PLACE_BLOCKS, "Follow place blocks",
                "Allow placing blocks to bridge a gap on the way to the target.", true);
        flag(KEEP_TARGET, "Follow keep target",
                "Stay on the target already being followed instead of switching to a "
                        + "closer one, so a crowd does not pull the follow about.", false);
        flag(CLOSEST_ONLY, "Follow closest only",
                "Work towards the single closest match instead of every match at once.", false);
        flag(IGNORE_SAME_KIND, "Follow ignore same kind",
                "Skip a closer match of the same kind as the one already being followed, so "
                        + "a crowd does not pull the follow between them.", false);
        flag(LOOK_AT_TARGET, "Look at target",
                "Turn the camera onto the target whenever HELM is standing still.", false);
        amount(MAX_LOOK_PITCH, "Follow max look pitch",
                "How far up or down the camera may tilt while watching, in degrees.", 80.0D,
                0.0D, 90.0D);
    }

    public int radius() {
        return level(RADIUS);
    }

    public int verticalRadius() {
        return level(VERTICAL_RADIUS);
    }

    public double offsetDistance() {
        return rate(OFFSET_DISTANCE);
    }

    public double offsetDirection() {
        return rate(OFFSET_DIRECTION);
    }

    public double verticalOffset() {
        return rate(VERTICAL_OFFSET);
    }

    public int maxTargetDistance() {
        return level(MAX_TARGET_DISTANCE);
    }

    public int minTargetDistance() {
        return level(MIN_TARGET_DISTANCE);
    }

    public int waitTicks() {
        return level(WAIT_TICKS);
    }

    public int retryTicks() {
        return level(RETRY_TICKS);
    }

    public int replanTicks() {
        return level(REPLAN_TICKS);
    }

    public boolean holdWhenClose() {
        return on(HOLD_WHEN_CLOSE);
    }

    public boolean sprint() {
        return on(SPRINT);
    }

    public boolean breakBlocks() {
        return on(BREAK_BLOCKS);
    }

    public boolean placeBlocks() {
        return on(PLACE_BLOCKS);
    }

    public boolean keepTarget() {
        return on(KEEP_TARGET);
    }

    public boolean closestOnly() {
        return on(CLOSEST_ONLY);
    }

    public boolean ignoreSameKind() {
        return on(IGNORE_SAME_KIND);
    }

    public boolean lookAtTarget() {
        return on(LOOK_AT_TARGET);
    }

    public double maxLookPitch() {
        return rate(MAX_LOOK_PITCH);
    }
}