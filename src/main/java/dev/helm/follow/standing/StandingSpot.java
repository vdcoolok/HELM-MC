package dev.helm.follow.standing;

import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public record StandingSpot(int x, int y, int z) {

    public static StandingSpot under(Vec3 where) {
        return new StandingSpot(Mth.floor(where.x), Mth.floor(where.y), Mth.floor(where.z));
    }

    public static StandingSpot from(Vec3 origin, double alongX, double alongZ, double rise) {
        return new StandingSpot(Mth.floor(origin.x + alongX),
                Mth.floor(origin.y + rise),
                Mth.floor(origin.z + alongZ));
    }

    public String label() {
        return x + " " + y + " " + z;
    }

    @Override
    public String toString() {
        return label();
    }
}