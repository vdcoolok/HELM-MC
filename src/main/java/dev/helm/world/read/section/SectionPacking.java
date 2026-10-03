package dev.helm.world.read.section;

import net.minecraft.util.BitStorage;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.Palette;

public record SectionPacking(Palette<BlockState> palette, BitStorage storage) {
}
