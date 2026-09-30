package dev.helm.aim;

public final class AimAngles {

    public static final double MAX_PITCH = 90.0D;

    private AimAngles() {
    }

    public static double wrapYaw(double value) {
        double wrapped = value % 360.0D;
        if (wrapped < -180.0D) {
            wrapped += 360.0D;
        }
        if (wrapped > 180.0D) {
            wrapped -= 360.0D;
        }
        return wrapped;
    }

    public static double clampPitch(double value) {
        return Math.max(-MAX_PITCH, Math.min(MAX_PITCH, value));
    }

    public static double unwrapYaw(double value) {
        double wrapped = wrapYaw(value);
        return value + (wrapped - value);
    }
}
