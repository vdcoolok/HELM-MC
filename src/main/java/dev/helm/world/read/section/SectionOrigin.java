package dev.helm.world.read.section;

import net.minecraft.core.BlockPos;

public record SectionOrigin(int chunkX, int baseY, int chunkZ) {

    public BlockPos at(int x, int y, int z) {
        return new BlockPos(chunkX + x, baseY + y, chunkZ + z);
    }

    public BlockPos at(int index) {
        return new BlockPos(chunkX + (index & 15), baseY + (index >> 8),
                chunkZ + ((index & 255) >> 4));
    }
}
