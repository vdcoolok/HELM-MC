package dev.helm.world.cache;

import java.util.List;
import java.util.Map;
import net.minecraft.world.level.block.state.BlockState;

public record RegionPayload(ChunkBitmap[][] bitmaps,
                            BlockState[][][] surfaces,
                            Map<String, List<int[]>>[][] tracked,
                            long[][] packedAt) {

    public boolean present(int x, int z) {
        return bitmaps[x][z] != null;
    }
}
