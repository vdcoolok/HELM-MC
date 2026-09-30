package dev.helm.pathfinding.world;

import net.minecraft.world.level.block.state.BlockState;

public interface BlockView extends WorldView {

    BlockState stateAt(int x, int y, int z);

    default BlockState stateAtOrNull(int x, int y, int z) {
        return loaded(x, z) ? stateAt(x, y, z) : null;
    }
}