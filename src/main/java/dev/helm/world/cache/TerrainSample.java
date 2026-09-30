package dev.helm.world.cache;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class TerrainSample {

    private TerrainSample() {
    }

    public static BlockState representative(TerrainKind kind,
                                            ResourceKey<Level> dimension) {
        return switch (kind) {
            case AIR -> Blocks.AIR.defaultBlockState();
            case WATER -> Blocks.WATER.defaultBlockState();
            case AVOID -> Blocks.LAVA.defaultBlockState();
            case SOLID -> solid(dimension);
        };
    }

    private static BlockState solid(ResourceKey<Level> dimension) {
        if (Level.NETHER.equals(dimension)) {
            return Blocks.NETHERRACK.defaultBlockState();
        }
        if (Level.END.equals(dimension)) {
            return Blocks.END_STONE.defaultBlockState();
        }
        return Blocks.STONE.defaultBlockState();
    }
}
