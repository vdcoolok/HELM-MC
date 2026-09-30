package dev.helm.aim;

import java.util.random.RandomGenerator;

public final class MouseScale {

    private static final float QUANTISE_CONSTANT = 0.15F;

    private final double sensitivity;

    public MouseScale(double sensitivity) {
        this.sensitivity = sensitivity;
    }

    public float anglePerPixel(float pixels) {
        double factor = sensitivity * 0.6D + 0.2D;
        return (float) (pixels * factor * factor * factor * 8.0D) * QUANTISE_CONSTANT;
    }

    public float pixelsForAngle(float degrees) {
        float smallest = anglePerPixel(1.0F);
        if (smallest == 0.0F) {
            return degrees;
        }
        return Math.round(degrees / smallest);
    }

    public float apply(float from, float to) {
        return from + anglePerPixel(pixelsForAngle(to - from));
    }

    public static MouseScale fromOptions(double sensitivity) {
        return new MouseScale(sensitivity);
    }

    public static RandomGenerator source() {
        return RandomGenerator.getDefault();
    }
}
