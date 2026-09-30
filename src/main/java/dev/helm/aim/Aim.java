package dev.helm.aim;

public record Aim(double yaw, double pitch) {

    public static final double EPSILON = 0.01D;

    public Aim {
        yaw = AimAngles.wrapYaw(yaw);
        pitch = AimAngles.clampPitch(pitch);
    }

    public Aim withPitch(double value) {
        return new Aim(yaw, value);
    }

    public Aim withYaw(double value) {
        return new Aim(value, pitch);
    }

    public Aim shifted(double yawBy, double pitchBy) {
        return new Aim(yaw + yawBy, pitch + pitchBy);
    }

    public boolean yawNear(Aim other) {
        double difference = Math.abs(AimAngles.wrapYaw(yaw) - AimAngles.wrapYaw(other.yaw));
        return difference < EPSILON || difference > 360.0D - EPSILON;
    }

    public boolean near(Aim other) {
        return yawNear(other) && Math.abs(pitch - other.pitch) < EPSILON;
    }

    public static Aim of(float yaw, float pitch) {
        return new Aim(yaw, pitch);
    }
}
