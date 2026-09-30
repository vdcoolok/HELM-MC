package dev.helm.world.cache;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.level.block.state.BlockState;

public final class RegionCodec {

    private static final int MAGIC = 0x1B2C3D4E;
    static final int GRID = 32;
    static final int COLUMNS = 16 * 16;

    private RegionCodec() {
    }

    public static void write(CachedRegion region, DataOutputStream out) throws IOException {
        PackedChunk[][] chunks = region.grid();
        int bitmapBytes = ChunkBitmap.byteSize(region.height());
        out.writeInt(MAGIC);
        writeBitmaps(chunks, bitmapBytes, out);
        writeSurfaces(chunks, out);
        writeTracked(chunks, out);
        writeTimestamps(chunks, out);
    }

    public static RegionPayload read(int height, DataInputStream in) throws IOException {
        if (in.readInt() != MAGIC) {
            throw new IOException("Not a region file");
        }
        int bitmapBytes = ChunkBitmap.byteSize(height);
        ChunkBitmap[][] bitmaps = readBitmaps(height, bitmapBytes, in);
        return new RegionPayload(bitmaps, readSurfaces(bitmaps, in),
                readTracked(bitmaps, in), readTimestamps(bitmaps, in));
    }

    private static void writeBitmaps(PackedChunk[][] chunks, int bitmapBytes,
                                     DataOutputStream out) throws IOException {
        for (int x = 0; x < GRID; x++) {
            for (int z = 0; z < GRID; z++) {
                PackedChunk chunk = chunks[x][z];
                if (chunk == null) {
                    out.writeByte(0);
                    continue;
                }
                out.writeByte(1);
                byte[] packed = chunk.bitmap().toBytes();
                out.write(packed);
                out.write(new byte[bitmapBytes - packed.length]);
            }
        }
    }

    private static void writeSurfaces(PackedChunk[][] chunks, DataOutputStream out)
            throws IOException {
        for (int x = 0; x < GRID; x++) {
            for (int z = 0; z < GRID; z++) {
                PackedChunk chunk = chunks[x][z];
                if (chunk == null) {
                    continue;
                }
                for (int column = 0; column < COLUMNS; column++) {
                    out.writeUTF(BlockNames.of(chunk.surfaceAt(column).getBlock()));
                }
            }
        }
    }

    private static void writeTracked(PackedChunk[][] chunks, DataOutputStream out)
            throws IOException {
        for (int x = 0; x < GRID; x++) {
            for (int z = 0; z < GRID; z++) {
                PackedChunk chunk = chunks[x][z];
                if (chunk == null) {
                    continue;
                }
                Map<String, List<int[]>> byName = chunk.trackedByName();
                out.writeInt(byName.size());
                for (Map.Entry<String, List<int[]>> entry : byName.entrySet()) {
                    out.writeUTF(entry.getKey());
                    out.writeInt(entry.getValue().size());
                    for (int[] position : entry.getValue()) {
                        out.writeByte((byte) ((position[2] << 4) | position[0]));
                        out.writeInt(position[1]);
                    }
                }
            }
        }
    }

    private static void writeTimestamps(PackedChunk[][] chunks, DataOutputStream out)
            throws IOException {
        for (int x = 0; x < GRID; x++) {
            for (int z = 0; z < GRID; z++) {
                PackedChunk chunk = chunks[x][z];
                if (chunk != null) {
                    out.writeLong(chunk.packedAt());
                }
            }
        }
    }

    private static ChunkBitmap[][] readBitmaps(int height, int bitmapBytes, DataInputStream in)
            throws IOException {
        ChunkBitmap[][] bitmaps = new ChunkBitmap[GRID][GRID];
        for (int x = 0; x < GRID; x++) {
            for (int z = 0; z < GRID; z++) {
                int flag = in.read();
                if (flag == 0) {
                    continue;
                }
                if (flag != 1) {
                    throw new IOException("Malformed region file");
                }
                byte[] packed = new byte[bitmapBytes];
                in.readFully(packed);
                bitmaps[x][z] = new ChunkBitmap(height, ChunkBitmap.bitsOf(packed));
            }
        }
        return bitmaps;
    }

    private static BlockState[][][] readSurfaces(ChunkBitmap[][] bitmaps, DataInputStream in)
            throws IOException {
        BlockState[][][] surfaces = new BlockState[GRID][GRID][];
        for (int x = 0; x < GRID; x++) {
            for (int z = 0; z < GRID; z++) {
                if (bitmaps[x][z] == null) {
                    continue;
                }
                BlockState[] surface = new BlockState[COLUMNS];
                for (int column = 0; column < COLUMNS; column++) {
                    surface[column] = BlockNames.required(in.readUTF()).defaultBlockState();
                }
                surfaces[x][z] = surface;
            }
        }
        return surfaces;
    }

    private static Map<String, List<int[]>>[][] readTracked(ChunkBitmap[][] bitmaps,
                                                            DataInputStream in)
            throws IOException {
        Map<String, List<int[]>>[][] tracked = new Map[GRID][GRID];
        for (int x = 0; x < GRID; x++) {
            for (int z = 0; z < GRID; z++) {
                if (bitmaps[x][z] == null) {
                    continue;
                }
                Map<String, List<int[]>> byName = new HashMap<>();
                int types = in.readInt();
                for (int type = 0; type < types; type++) {
                    String name = in.readUTF();
                    BlockNames.required(name);
                    int count = in.readInt();
                    List<int[]> positions = new ArrayList<>(count);
                    for (int entry = 0; entry < count; entry++) {
                        int packed = in.readByte();
                        positions.add(new int[]{packed & 0x0F, in.readInt(), (packed >> 4) & 0x0F});
                    }
                    byName.put(name, positions);
                }
                tracked[x][z] = byName;
            }
        }
        return tracked;
    }

    private static long[][] readTimestamps(ChunkBitmap[][] bitmaps, DataInputStream in)
            throws IOException {
        long[][] packedAt = new long[GRID][GRID];
        for (int x = 0; x < GRID; x++) {
            for (int z = 0; z < GRID; z++) {
                if (bitmaps[x][z] != null) {
                    packedAt[x][z] = in.readLong();
                }
            }
        }
        return packedAt;
    }
}
