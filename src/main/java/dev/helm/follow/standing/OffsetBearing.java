package dev.helm.follow.standing;

import net.minecraft.util.Mth;

public final class OffsetBearing {

    private OffsetBearing() {
    }

    public static StandingSpot behind(Target target, double directionDegrees, double distance,
                                      double rise) {
        if (distance <= 0.0D && rise == 0.0D) {
            return StandingSpot.under(target.entity().position());
        }
        float theta = (float) Math.toRadians(directionDegrees);
        double alongX = distance <= 0.0D ? 0.0D : -Mth.sin(theta) * distance;
        double alongZ = distance <= 0.0D ? 0.0D : Mth.cos(theta) * distance;
        return StandingSpot.from(target.entity().position(), alongX, alongZ, rise);
    }
}