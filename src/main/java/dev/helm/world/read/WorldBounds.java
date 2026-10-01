package dev.helm.world.read;

import net.minecraft.world.level.border.WorldBorder;

public final class WorldBounds {

    private final double minX;
    private final double maxX;
    private final double minZ;
    private final double maxZ;

    public WorldBounds(WorldBorder border) {
        this.minX = border.getMinX();
        this.maxX = border.getMaxX();
        this.minZ = border.getMinZ();
        this.maxZ = border.getMaxZ();
    }

    public WorldBounds(double minX, double maxX, double minZ, double maxZ) {
        this.minX = minX;
        this.maxX = maxX;
        this.minZ = minZ;
        this.maxZ = maxZ;
    }

    public boolean entirelyContains(int x, int z) {
        return x + 1 > minX && x < maxX
                && z + 1 > minZ && z < maxZ;
    }

    public boolean canPlaceAt(int x, int z) {
        return x > minX && x + 1 < maxX
                && z > minZ && z + 1 < maxZ;
    }
}