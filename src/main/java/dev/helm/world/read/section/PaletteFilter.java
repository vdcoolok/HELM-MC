package dev.helm.world.read.section;

import java.util.Set;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.GlobalPalette;
import net.minecraft.world.level.chunk.Palette;

public final class PaletteFilter {

    private final Set<Block> wanted;

    public PaletteFilter(Set<Block> wanted) {
        this.wanted = wanted;
    }

    public boolean wants(BlockState state) {
        return wanted.contains(state.getBlock());
    }

    public SectionMatcher matcher(Palette<BlockState> palette) {
        if (palette instanceof GlobalPalette<?>) {
            return new GlobalPaletteMatcher(wanted);
        }
        int size = palette.getSize();
        if (size == 0) {
            return null;
        }
        boolean[] accepted = new boolean[size];
        int matched = 0;
        for (int index = 0; index < size; index++) {
            if (wants(palette.valueFor(index))) {
                accepted[index] = true;
                matched++;
            }
        }
        return matched == 0 ? null : new LocalPaletteMatcher(accepted);
    }
}
