package dev.helm.world.read;

import net.minecraft.world.level.Level;

public record BuildRange(int lowestLevel, int levelCount) {

    public static BuildRange of(Level level) {
        return new BuildRange(level.dimensionType().minY(), level.dimensionType().height());
    }

    public int ceilingLevel() {
        return lowestLevel + levelCount;
    }

    public boolean contains(int y) {
        return y >= lowestLevel && y < ceilingLevel();
    }

    public int sectionIndex(int y) {
        return y - lowestLevel;
    }

    public int blockInSection(int y) {
        return (y - lowestLevel) & 15;
    }
}
