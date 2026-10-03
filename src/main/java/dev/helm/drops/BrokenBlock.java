package dev.helm.drops;

import net.minecraft.core.BlockPos;

public record BrokenBlock(BlockPos pos, long tick) {

    public boolean olderThan(long tick, long window) {
        return tick - this.tick >= window;
    }

    public boolean near(double x, double y, double z, double radius) {
        double dx = x - pos.getX();
        double dy = y - pos.getY();
        double dz = z - pos.getZ();
        return dx * dx + dy * dy + dz * dz <= radius * radius;
    }
}
