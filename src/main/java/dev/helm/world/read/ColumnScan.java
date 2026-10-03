package dev.helm.world.read;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;

import dev.helm.world.read.section.PaletteFilter;
import dev.helm.world.read.section.SectionOrigin;
import dev.helm.world.read.section.SectionScan;

final class ColumnScan {

    private ColumnScan() {
    }

    public static void into(ChunkAccess chunk, int chunkX, int chunkZ, SectionOrder order,
                            PaletteFilter filter, List<BlockPos> found) {
        LevelChunkSection[] sections = chunk.getSections();
        int lowest = chunk.getMinY();
        while (order.hasNext()) {
            int index = order.next();
            if (index >= sections.length) {
                continue;
            }
            SectionScan.into(sections[index],
                    new SectionOrigin(chunkX, lowest + (index << 4), chunkZ), filter, found);
        }
    }
}
