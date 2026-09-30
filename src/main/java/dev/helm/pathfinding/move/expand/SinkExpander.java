package dev.helm.pathfinding.move.expand;

import dev.helm.pathfinding.cost.FallCosts;
import dev.helm.pathfinding.cost.MoveCosts;
import dev.helm.pathfinding.move.MoveEnvironment;
import dev.helm.pathfinding.move.MoveExpander;
import dev.helm.pathfinding.move.MoveTarget;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class SinkExpander implements MoveExpander {

    private final MoveEnvironment env;

    public SinkExpander(MoveEnvironment env) {
        this.env = env;
    }

    @Override
    public MoveTarget expand(int x, int y, int z, MoveTarget out) {
        if (!env.tuning().downwardAllowed()) {
            out.cost = MoveCosts.IMPOSSIBLE;
            return out;
        }
        if (!env.walk().onTop(x, y - 2, z)) {
            out.cost = MoveCosts.IMPOSSIBLE;
            return out;
        }
        BlockState down = env.stateAt(x, y - 1, z);
        if (down.getBlock() == Blocks.LADDER || down.getBlock() == Blocks.VINE) {
            out.cost = MoveCosts.LADDER_DOWN_ONE;
            return out;
        }
        out.cost = FallCosts.forDistance(1)
                + env.work().breakTicks(x, y - 1, z, down, false);
        return out;
    }
}