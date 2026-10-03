package dev.helm.world.read;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.LevelChunkSection;

import dev.helm.world.read.section.PaletteFilter;
import dev.helm.world.read.section.SectionOrigin;
import dev.helm.world.read.section.SectionScan;

final class ColumnScan {

    private ColumnScan() {
    }

    public static void into(LevelChunkSection[] sections, int lowest, int chunkX, int chunkZ,
                            SectionOrder order, PaletteFilter filter, List<BlockPos> found) {
        for (int at = 0; at < order.length(); at++) {
            int index = order.at(at);
            if (index >= sections.length) {
                continue;
            }
            SectionScan.into(sections[index],
                    new SectionOrigin(chunkX, lowest + (index << 4), chunkZ), filter, found);
        }
    }
}
