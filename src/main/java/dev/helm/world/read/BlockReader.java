package dev.helm.world.read;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import dev.helm.world.cache.CachedRegion;
import dev.helm.world.cache.RegionStore;
import dev.helm.world.cache.WorldCache;

public final class BlockReader {

    public static final BlockState AIR = Blocks.AIR.defaultBlockState();

    private final BuildRange range;
    private final ChunkSource chunks;
    private final boolean preferLoaded;
    private final boolean cachingOn;
    private CachedRegion region;
    private int regionX;
    private int regionZ;
    private LevelChunk recent;
    private int recentX = Integer.MIN_VALUE;
    private int recentZ = Integer.MIN_VALUE;

    public BlockReader(ClientLevel level, boolean preferLoaded, boolean cachingOn) {
        this(BuildRange.of(level), new ChunkLookup(level), preferLoaded, cachingOn);
    }

    public BlockReader(BuildRange range, ChunkSource chunks, boolean preferLoaded,
                       boolean cachingOn) {
        this.range = range;
        this.chunks = chunks;
        this.preferLoaded = preferLoaded;
        this.cachingOn = cachingOn;
    }

    public BuildRange range() {
        return range;
    }

    public BlockState state(int x, int y, int z) {
        int section = range.sectionIndex(y);
        if (section < 0 || section >= range.levelCount()) {
            return AIR;
        }
        if (preferLoaded) {
            int chunkX = x >> 4;
            int chunkZ = z >> 4;
            LevelChunk chunk = recent;
            if (chunk == null || recentX != chunkX || recentZ != chunkZ) {
                chunk = chunks.loaded(chunkX, chunkZ);
                if (chunk == null) {
                    return cached(x, y, z);
                }
                recent = chunk;
                recentX = chunkX;
                recentZ = chunkZ;
            }
            return fromChunk(chunk, x, section, z);
        }
        return cached(x, y, z);
    }

    public boolean loaded(int x, int z) {
        if (chunks.loaded(x >> 4, z >> 4) != null) {
            return true;
        }
        return regionAt(x, z) != null;
    }

    public boolean residentChunk(int x, int z) {
        return chunks.resident(x >> 4, z >> 4);
    }

    private BlockState cached(int x, int y, int z) {
        if (!cachingOn) {
            return AIR;
        }
        CachedRegion found = regionAt(x, z);
        if (found == null) {
            return AIR;
        }
        BlockState state = found.stateAt(x, y, z);
        return state == null ? AIR : state;
    }

    private CachedRegion regionAt(int x, int z) {
        if (!cachingOn) {
            return null;
        }
        int wantX = x >> 9;
        int wantZ = z >> 9;
        if (region != null && regionX == wantX && regionZ == wantZ) {
            return region;
        }
        RegionStore store = store();
        if (store == null) {
            return null;
        }
        region = store.region(wantX, wantZ);
        regionX = wantX;
        regionZ = wantZ;
        return region;
    }

    private RegionStore store() {
        WorldCache cache = WorldCache.get();
        return cache == null ? null : cache.store();
    }

    private BlockState fromChunk(LevelChunk chunk, int x, int section, int z) {
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
}
