package dev.helm.world.read.section;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;

public final class SectionScan {

    private SectionScan() {
    }

    public static void into(LevelChunkSection section, SectionOrigin origin, PaletteFilter filter,
                            List<BlockPos> found) {
        if (section == null || section.hasOnlyAir()) {
            return;
        }
        PalettedContainer<BlockState> states = section.getStates();
        SectionPacking packing = ContainerReading.of(states);
        if (packing == null) {
            eachBlock(states, origin, filter, found);
            return;
        }
        SectionMatcher matcher = filter.matcher(packing.palette());
        if (matcher == null) {
            return;
        }
        if (matcher.fills()) {
            everyBlock(origin, found);
            return;
        }
        PackedWords.collect(packing.storage(), matcher, origin, found);
    }

    private static void eachBlock(PalettedContainer<BlockState> states, SectionOrigin origin,
                                  PaletteFilter filter, List<BlockPos> found) {
        for (int y = 0; y < 16; y++) {
            for (int z = 0; z < 16; z++) {
                for (int x = 0; x < 16; x++) {
                    if (filter.wants(states.get(x, y, z))) {
                        found.add(origin.at(x, y, z));
                    }
                }
            }
        }
    }

    private static void everyBlock(SectionOrigin origin, List<BlockPos> found) {
        for (int y = 0; y < 16; y++) {
            for (int z = 0; z < 16; z++) {
                for (int x = 0; x < 16; x++) {
                    found.add(origin.at(x, y, z));
                }
            }
        }
    }
}
