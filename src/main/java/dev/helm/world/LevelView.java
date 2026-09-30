package dev.helm.world;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public final class LevelView {

    private LevelView() {
    }

    public static BlockState stateAt(Level level, int x, int y, int z) {
        return level.getBlockState(new BlockPos(x, y, z));
    }

    public static boolean chunkLoaded(Level level, int x, int z) {
        return level.isLoaded(new BlockPos(x, 0, z));
    }

    public static int lowestLevel(Level level) {
        return level.getMinY();
    }

    public static int levelCount(Level level) {
        return level.getHeight();
    }

    public static int ceiling(Level level) {
        return level.getMinY() + level.getHeight();
    }

    public static boolean insideBorder(Level level, int x, int y, int z) {
        return y >= level.getMinY()
                && y < level.getMinY() + level.getHeight()
                && level.getWorldBorder().isWithinBounds(x, y, z);
    }
}
