package dev.helm.rotation;

public record Rotation(double yaw, double pitch) {

    public static final double MAX_PITCH = 90.0D;

    public Rotation {
        yaw = normalizeYaw(yaw);
        pitch = clampPitch(pitch);
    }

    public static Rotation of(double pitch, double yaw) {
        return new Rotation(yaw, pitch);
    }

    public static double normalizeYaw(double value) {
        double wrapped = value % 360.0D;
        if (wrapped >= 180.0D) {
            wrapped -= 360.0D;
        }
        if (wrapped < -180.0D) {
            wrapped += 360.0D;
        }
        return wrapped;
    }

    public static double clampPitch(double value) {
        return Math.max(-MAX_PITCH, Math.min(MAX_PITCH, value));
    }

    public double yawDegrees() {
        return yaw;
    }

    public double pitchDegrees() {
        return pitch;
    }
}
