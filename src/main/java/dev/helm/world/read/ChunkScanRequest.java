package dev.helm.world.read;

import java.util.Set;

import net.minecraft.world.level.block.Block;

public record ChunkScanRequest(Set<Block> wanted, int maxResults, int chunkRadius,
                               int levelWindow) {
}