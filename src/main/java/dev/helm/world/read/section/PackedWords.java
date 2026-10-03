package dev.helm.world.read.section;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.util.BitStorage;

public final class PackedWords {

    private PackedWords() {
    }

    public static void collect(BitStorage storage, SectionMatcher matcher, SectionOrigin origin,
                               List<BlockPos> found) {
        long[] words = storage.getRaw();
        int bits = storage.getBits();
        int entries = storage.getSize();
        if (bits < 1 || words.length == 0) {
            return;
        }
        long mask = (1L << bits) - 1L;
        int index = 0;
        for (int word = 0; word < words.length && index < entries; word++) {
            long packed = words[word];
            for (int shift = 0; shift <= 64 - bits && index < entries; shift += bits, index++) {
                if (matcher.accepts((int) ((packed >>> shift) & mask))) {
                    found.add(origin.at(index));
                }
            }
        }
    }
}
