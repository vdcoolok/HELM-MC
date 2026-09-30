package dev.helm.world.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.DimensionType;
import dev.helm.world.read.BlockReader;

public final class ChunkPacker {

    private ChunkPacker() {
    }

    public static PackedChunk pack(LevelChunk chunk) {
        DimensionType dimension = chunk.getLevel().dimensionType();
        int floor = dimension.minY();
        int height = dimension.height();
        ChunkBitmap bitmap = new ChunkBitmap(height);
        Map<String, List<int[]>> tracked = new HashMap<>();
        TerrainClassifier reader = new TerrainClassifier(chunk);
        for (int y = 0; y < height; y++) {
            for (int z = 0; z < 16; z++) {
                for (int x = 0; x < 16; x++) {
                    BlockState state = reader.read(x, y + floor, z);
                    bitmap.set(x, y, z, reader.at(x, y + floor, z));
                    record(tracked, state.getBlock(), floor, x, y, z);
                }
            }
        }
        return new PackedChunk(chunk.getPos().x(), chunk.getPos().z(), height, floor, bitmap,
                surface(bitmap, reader, floor), tracked, System.currentTimeMillis());
    }

    private static void record(Map<String, List<int[]>> tracked, Block block, int floor,
                              int x, int y, int z) {
        if (!TrackedBlocks.is(block)) {
            return;
        }
        tracked.computeIfAbsent(BlockNames.of(block), name -> new ArrayList<>())
                .add(new int[]{x, y + floor, z});
    }

    private static BlockState[] surface(ChunkBitmap bitmap, TerrainClassifier reader, int floor) {
        BlockState[] surface = new BlockState[256];
        for (int z = 0; z < 16; z++) {
            for (int x = 0; x < 16; x++) {
                surface[ChunkBitmap.columnIndex(x, z)] = topOf(bitmap, reader, floor, x, z);
            }
        }
        return surface;
    }

    private static BlockState topOf(ChunkBitmap bitmap, TerrainClassifier reader, int floor,
                                    int x, int z) {
        for (int y = bitmap.height() - 1; y >= 0; y--) {
            if (bitmap.get(x, y, z).occupied()) {
                return reader.read(x, y + floor, z);
            }
        }
        return BlockReader.AIR;
    }
}
