package dev.helm.setting;

public final class LookSettings extends SettingSection {

    public static final String FREE_LOOK = "look.freeLook";
    public static final String BLOCK_FREE_LOOK = "look.blockFreeLook";
    public static final String ELYTRA_FREE_LOOK = "look.elytraFreeLook";
    public static final String SMOOTH_LOOK = "look.smoothLook";
    public static final String ELYTRA_SMOOTH_LOOK = "look.elytraSmoothLook";
    public static final String SMOOTH_LOOK_TICKS = "look.smoothLookTicks";
    public static final String REMAIN_WITH_LOOK = "look.remainWithExistingLookDirection";
    public static final String ANTI_CHEAT = "look.antiCheatCompatibility";
    public static final String RANDOM_LOOKING = "look.randomLooking";
    public static final String RANDOM_LOOKING_WOBBLE = "look.randomLooking113";
    public static final String BLOCK_REACH = "look.blockReachDistance";

    public LookSettings() {
        flag(FREE_LOOK, "Free look",
                "Send rotations to the server without moving the camera.", true);
        flag(BLOCK_FREE_LOOK, "Free look while breaking",
                "Free look stays on while a block is being mined.", false);
        flag(ELYTRA_FREE_LOOK, "Free look while gliding",
                "Free look stays on while gliding.", true);
        flag(SMOOTH_LOOK, "Smooth look",
                "Camera yaw follows an average of recent server yaw.", false);
        flag(ELYTRA_SMOOTH_LOOK, "Smooth look while gliding",
                "Camera follows the average while gliding.", false);
        count(SMOOTH_LOOK_TICKS, "Smooth look ticks",
                "How many recent rotations the camera averages.", 5, 1, 100);
        flag(REMAIN_WITH_LOOK, "Keep look direction",
                "Prefer the direction the player is already facing.", true);
        flag(ANTI_CHEAT, "Anti cheat compatibility",
                "Rotations are sent to the server rather than applied on the client.", true);
        amount(RANDOM_LOOKING, "Random looking",
                "Degrees of random yaw and pitch added every tick.", 0.01D, 0.0D, 30.0D);
        amount(RANDOM_LOOKING_WOBBLE, "Random looking wobble",
                "Occasional larger random yaw offset.", 2.0D, 0.0D, 30.0D);
        amount(BLOCK_REACH, "Block reach",
                "How far away a block may be and still be mined.", 4.5D, 1.0D, 6.0D);
    }

    public boolean freeLook() {
        return on(FREE_LOOK);
    }

    public boolean blockFreeLook() {
        return on(BLOCK_FREE_LOOK);
    }

    public boolean elytraFreeLook() {
        return on(ELYTRA_FREE_LOOK);
    }

    public boolean smoothLook() {
        return on(SMOOTH_LOOK);
    }

    public boolean elytraSmoothLook() {
        return on(ELYTRA_SMOOTH_LOOK);
    }

    public int smoothLookTicks() {
        return level(SMOOTH_LOOK_TICKS);
    }

    public boolean remainWithLookDirection() {
        return on(REMAIN_WITH_LOOK);
    }

    public boolean antiCheatCompatibility() {
        return on(ANTI_CHEAT);
    }

    public double randomLooking() {
        return rate(RANDOM_LOOKING);
    }

    public double randomLookingWobble() {
        return rate(RANDOM_LOOKING_WOBBLE);
    }

    public double blockReachDistance() {
        return rate(BLOCK_REACH);
    }
}
