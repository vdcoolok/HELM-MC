package dev.helm.world.cache;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public final class RegionFile {

    private static final int BUFFER = 32768;

    private RegionFile() {
    }

    public static Path pathOf(Path directory, int regionX, int regionZ) {
        return directory.resolve("r." + regionX + "." + regionZ + ".rcache");
    }

    public static boolean exists(Path directory, int regionX, int regionZ) {
        return Files.isRegularFile(pathOf(directory, regionX, regionZ));
    }

    public static void write(Path directory, CachedRegion region) throws IOException {
        Files.createDirectories(directory);
        Path file = pathOf(directory, region.regionX(), region.regionZ());
        try (OutputStream raw = Files.newOutputStream(file);
             GZIPOutputStream gzip = new GZIPOutputStream(new BufferedOutputStream(raw),
                     BUFFER);
             DataOutputStream out = new DataOutputStream(gzip)) {
            RegionCodec.write(region, out);
        }
    }

    public static CachedRegion read(Path directory, CachedRegion region) throws IOException {
        Path file = pathOf(directory, region.regionX(), region.regionZ());
        try (InputStream raw = Files.newInputStream(file);
             GZIPInputStream gzip = new GZIPInputStream(new BufferedInputStream(raw),
                     BUFFER);
             DataInputStream in = new DataInputStream(gzip)) {
            region.absorb(RegionCodec.read(region.height(), in));
        }
        return region;
    }
}
