package dev.helm.pathfinding.move;

import dev.helm.pathfinding.context.Tunables;
import dev.helm.pathfinding.cost.MoveCosts;
import dev.helm.pathfinding.world.BlockView;
import dev.helm.pathfinding.world.block.WalkRules;
import dev.helm.pathfinding.world.block.WorkCosts;
import net.minecraft.world.level.block.state.BlockState;

public interface MoveEnvironment extends BlockView {

    WalkRules walk();

    WorkCosts work();

    Tunables tuning();

    default BlockState at(int x, int y, int z) {
        return stateAt(x, y, z);
    }

    default double impossible() {
        return MoveCosts.IMPOSSIBLE;
    }

    default int ceiling() {
        return lowestLevel() + levelCount();
    }
}