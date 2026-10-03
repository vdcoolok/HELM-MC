package dev.helm.world.read;

import java.util.Set;

import net.minecraft.world.level.block.Block;

public record SweepRequest(Set<Block> wanted, int resultsWanted, int chunkRadius) {
}
