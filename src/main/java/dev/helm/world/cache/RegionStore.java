package dev.helm.world.cache;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import dev.helm.diag.Trace;

public final class RegionStore {

    private static final int REGION_LIMIT = 30_000_000 / 512 + 1;
    private static final double PRUNE_DISTANCE = 1024.0D;

    private final Path directory;
    private final DimensionType dimension;
    private final ResourceKey<Level> dimensionId;
    private final Map<Long, CachedRegion> regions = new HashMap<>();
    private int packedChunks;
    private long lastSave;

    public RegionStore(Path directory, DimensionType dimension, ResourceKey<Level> dimensionId) {
        this.directory = directory;
        this.dimension = dimension;
        this.dimensionId = dimensionId;
    }

    public CachedRegion region(int regionX, int regionZ) {
        long key = keyOf(regionX, regionZ);
        synchronized (this) {
            return regions.get(key);
        }
    }

    public CachedRegion open(int regionX, int regionZ) {
        long key = keyOf(regionX, regionZ);
        synchronized (this) {
            CachedRegion existing = regions.get(key);
            if (existing != null) {
                return existing;
            }
            CachedRegion opened = new CachedRegion(regionX, regionZ, dimension, dimensionId);
            load(opened);
            regions.put(key, opened);
            return opened;
        }
    }

    public void store(PackedChunk chunk) {
        CachedRegion region = open(chunk.chunkX() >> 5, chunk.chunkZ() >> 5);
        region.put(chunk.chunkX() & 31, chunk.chunkZ() & 31, chunk);
        synchronized (this) {
            packedChunks++;
        }
    }

    public int packedChunks() {
        synchronized (this) {
            return packedChunks;
        }
    }

    public int regionCount() {
        synchronized (this) {
            return regions.size();
        }
    }

    public void expire(long oldestAcceptable) {
        for (CachedRegion region : all()) {
            region.dropExpired(oldestAcceptable);
        }
    }

    public void prune(int centreX, int centreZ) {
        synchronized (this) {
            for (Map.Entry<Long, CachedRegion> entry : new ArrayList<>(regions.entrySet())) {
                CachedRegion region = entry.getValue();
                double dx = (region.minX() + 256) - centreX;
                double dz = (region.minZ() + 256) - centreZ;
                if (Math.sqrt(dx * dx + dz * dz) > PRUNE_DISTANCE) {
                    regions.remove(entry.getKey());
                    Trace.instance().pulse("prune", "cache", "dropped region "
                            + region.regionX() + "," + region.regionZ() + " from memory");
                }
            }
        }
    }

    public void save() {
        long began = System.currentTimeMillis();
        for (CachedRegion region : all()) {
            if (!region.unsaved()) {
                continue;
            }
            try {
                RegionFile.write(directory, region);
                region.markSaved();
            } catch (IOException failure) {
                Trace.instance().event("cache", "could not save region "
                        + region.regionX() + "," + region.regionZ() + ": " + failure);
            }
        }
        lastSave = System.currentTimeMillis() - began;
        Trace.instance().event("cache", "saved " + regions.size() + " regions in " + lastSave
                + "ms, " + packedChunks + " chunks held");
    }

    private void load(CachedRegion region) {
        if (!RegionFile.exists(directory, region.regionX(), region.regionZ())) {
            return;
        }
        try {
            RegionFile.read(directory, region);
            Trace.instance().event("cache", "loaded region " + region.regionX() + ","
                    + region.regionZ() + " with " + region.packedChunks() + " chunks");
        } catch (IOException | RuntimeException corrupt) {
            Trace.instance().event("cache", "region " + region.regionX() + "," + region.regionZ()
                    + " could not be read and was ignored: " + corrupt);
        }
    }

    private List<CachedRegion> all() {
        synchronized (this) {
            return new ArrayList<>(regions.values());
        }
    }

    private static long keyOf(int regionX, int regionZ) {
        if (Math.abs(regionX) > REGION_LIMIT || Math.abs(regionZ) > REGION_LIMIT) {
            return 0L;
        }
        return (regionX & 0xFFFFFFFFL) | ((regionZ & 0xFFFFFFFFL) << 32);
    }
}
