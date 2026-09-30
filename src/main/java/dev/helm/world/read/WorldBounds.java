package dev.helm.world.read;

import net.minecraft.world.level.border.WorldBorder;

public final class WorldBounds {

    private final WorldBorder border;

    public WorldBounds(WorldBorder border) {
        this.border = border;
    }

    public boolean entirelyContains(int x, int z) {
        return x + 1 > border.getMinX() && x < border.getMaxX()
                && z + 1 > border.getMinZ() && z < border.getMaxZ();
    }

    public boolean canPlaceAt(int x, int z) {
        return x > border.getMinX() && x + 1 < border.getMaxX()
                && z > border.getMinZ() && z + 1 < border.getMaxZ();
    }
}
