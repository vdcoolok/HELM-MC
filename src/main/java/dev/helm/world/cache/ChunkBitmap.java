package dev.helm.world.cache;

import java.util.BitSet;

public final class ChunkBitmap {

    private static final int COLUMN_SHIFT = 5;
    private static final int LEVEL_SHIFT = 9;

    private final int height;
    private final BitSet bits;

    public ChunkBitmap(int height) {
        this(height, new BitSet(byteSize(height)));
    }

    public ChunkBitmap(int height, BitSet bits) {
        this.height = height;
        this.bits = bits;
    }

    public static int byteSize(int height) {
        return 2 * 16 * 16 * height / 8;
    }

    public static int positionIndex(int x, int y, int z) {
        return (y << LEVEL_SHIFT) | (z << COLUMN_SHIFT) | (x << 1);
    }

    public static int columnIndex(int x, int z) {
        return (z << 4) | x;
    }

    public int height() {
        return height;
    }

    public void set(int x, int y, int z, TerrainKind kind) {
        int index = positionIndex(x, y, z);
        bits.set(index, kind.high());
        bits.set(index + 1, kind.low());
    }

    public TerrainKind get(int x, int y, int z) {
        int index = positionIndex(x, y, z);
        return TerrainKind.of(bits.get(index), bits.get(index + 1));
    }

    public int highestOccupied(int x, int z) {
        for (int y = height - 1; y >= 0; y--) {
            if (get(x, y, z).occupied()) {
                return y;
            }
        }
        return 0;
    }

    public byte[] toBytes() {
        return bits.toByteArray();
    }

    public static BitSet bitsOf(byte[] bytes) {
        return BitSet.valueOf(bytes);
    }
}
