package dev.helm.outline.block;

import java.util.Collection;

import net.minecraft.core.BlockPos;

public interface BlockSilhouetteFeed {

    Collection<BlockPos> watch();

    int colour();
}
