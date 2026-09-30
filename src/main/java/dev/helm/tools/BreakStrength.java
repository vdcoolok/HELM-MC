package dev.helm.tools;

import net.minecraft.world.level.block.Block;

public interface BreakStrength {

    double against(Block block);

    void invalidate();
}
