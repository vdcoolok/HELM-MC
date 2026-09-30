package dev.helm.world.read;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;

public final class BlockReader {

    public static final BlockState AIR = Blocks.AIR.defaultBlockState();

    private final BuildRange range;
    private final ChunkLookup chunks;

    public BlockReader(ClientLevel level) {
        this(BuildRange.of(level), new ChunkLookup(level));
    }

    public BlockReader(BuildRange range, ChunkLookup chunks) {
        this.range = range;
        this.chunks = chunks;
    }

    public BuildRange range() {
        return range;
    }

    public BlockState state(int x, int y, int z) {
        int section = range.sectionIndex(y);
        if (section < 0 || section >= range.levelCount()) {
            return AIR;
        }
        LevelChunk chunk = chunks.loaded(x >> 4, z >> 4);
        if (chunk == null) {
            return AIR;
        }
        LevelChunkSection[] sections = chunk.getSections();
        int index = section >> 4;
        if (index < 0 || index >= sections.length) {
            return AIR;
        }
        LevelChunkSection at = sections[index];
        if (at == null || at.hasOnlyAir()) {
            return AIR;
        }
        return at.getBlockState(x & 15, section & 15, z & 15);
    }

    public boolean loaded(int x, int z) {
        return chunks.loaded(x >> 4, z >> 4) != null;
    }

    public boolean residentChunk(int x, int z) {
        return chunks.resident(x >> 4, z >> 4);
    }
}
