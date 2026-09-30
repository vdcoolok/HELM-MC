package dev.helm.pathfinding.move.expand;

import dev.helm.pathfinding.cost.FallCosts;
import dev.helm.pathfinding.cost.MoveCosts;
import dev.helm.pathfinding.move.MoveEnvironment;
import dev.helm.pathfinding.move.MoveExpander;
import dev.helm.pathfinding.move.MoveTarget;
import dev.helm.pathfinding.world.block.Climbable;
import dev.helm.pathfinding.world.block.LiquidRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class DropExpander implements MoveExpander {

    private final MoveEnvironment env;

    public DropExpander(MoveEnvironment env) {
        this.env = env;
    }

    @Override
    public MoveTarget expand(int x, int y, int z, MoveTarget out) {
        int toX = out.x;
        int toZ = out.z;
        double total = 0;

        BlockState underDest = env.stateAt(toX, y - 1, toZ);
        total += env.work().breakTicks(toX, y - 1, toZ, underDest, false);
        if (total >= MoveCosts.IMPOSSIBLE) {
            out.cost = MoveCosts.IMPOSSIBLE;
            return out;
        }
        total += env.work().breakTicks(toX, y, toZ, false);
        if (total >= MoveCosts.IMPOSSIBLE) {
            out.cost = MoveCosts.IMPOSSIBLE;
            return out;
        }
        total += env.work().breakTicks(toX, y + 1, toZ, true);
        if (total >= MoveCosts.IMPOSSIBLE) {
            out.cost = MoveCosts.IMPOSSIBLE;
            return out;
        }

        Block underSrcBlock = env.stateAt(x, y - 1, z).getBlock();
        if (Climbable.is(underSrcBlock)) {
            out.cost = MoveCosts.IMPOSSIBLE;
            return out;
        }

        BlockState twoBelow = env.stateAt(toX, y - 2, toZ);
        if (!env.walk().onTop(toX, y - 2, toZ, twoBelow)) {
            longFall(x, y, z, toX, toZ, total, twoBelow, out);
            return out;
        }
        if (underDest.getBlock() == Blocks.LADDER || underDest.getBlock() == Blocks.VINE) {
            out.cost = MoveCosts.IMPOSSIBLE;
            return out;
        }
        if (env.walk().frostWalkerTurns(underDest)) {
            out.cost = MoveCosts.IMPOSSIBLE;
            return out;
        }

        double walk = MoveCosts.WALK_OFF_BLOCK;
        if (underSrcBlock == Blocks.SOUL_SAND) {
            walk *= MoveCosts.WALK_ONE_OVER_SOUL_SAND / MoveCosts.WALK_ONE;
        }
        out.cost = total + walk + Math.max(FallCosts.forDistance(1), MoveCosts.CENTER_AFTER_FALL);
        out.y = y - 1;
        return out;
    }

    private void longFall(int x, int y, int z, int toX, int toZ, double frontBreak,
                          BlockState below, MoveTarget out) {
        if (frontBreak != 0
                && env.stateAt(toX, y + 2, toZ).getBlock() instanceof FallingBlock) {
            out.cost = MoveCosts.IMPOSSIBLE;
            return;
        }
        if (!env.walk().through(toX, y - 2, toZ, below)) {
            out.cost = MoveCosts.IMPOSSIBLE;
            return;
        }
        double spent = 0;
        for (int height = 3; ; height++) {
            int newY = y - height;
            if (newY < env.lowestLevel()) {
                out.cost = MoveCosts.IMPOSSIBLE;
                return;
            }
            boolean farEnough = height >= env.tuning().minFallHeight();
            BlockState onto = env.stateAt(toX, newY, toZ);
            int bare = Math.min(height, env.tuning().maxFallHeightNoWater());
            double tentative = MoveCosts.WALK_OFF_BLOCK + FallCosts.forDistance(bare)
                    + frontBreak + spent;

            if (farEnough && LiquidRules.water(onto)) {
                if (!env.walk().through(toX, newY, toZ, onto)
                        || env.tuning().assumeWalkOnWater()
                        || LiquidRules.source(toX, newY, toZ, onto, env::stateAt)
                        || !env.walk().onTop(toX, newY - 1, toZ)) {
                    out.cost = MoveCosts.IMPOSSIBLE;
                    return;
                }
                out.x = toX;
                out.y = newY;
                out.z = toZ;
                out.cost = tentative;
                return;
            }
            if (farEnough && env.tuning().allowFallIntoLava() && LiquidRules.lava(onto)) {
                out.x = toX;
                out.y = newY;
                out.z = toZ;
                out.cost = tentative;
                return;
            }
            if (bare <= 11 && Climbable.is(onto.getBlock())) {
                out.cost = tentative + MoveCosts.LADDER_UP_ONE;
                out.x = toX;
                out.y = newY;
                out.z = toZ;
                return;
            }
            if (!env.walk().through(toX, newY, toZ, onto)) {
                out.cost = MoveCosts.IMPOSSIBLE;
                return;
            }
            if (!env.walk().onTop(toX, newY - 1, toZ)) {
                out.cost = MoveCosts.IMPOSSIBLE;
                return;
            }
            spent = tentative;
            out.x = toX;
            out.y = newY;
            out.z = toZ;
            out.cost = tentative;
            if (height >= env.tuning().maxFallHeightNoWater()) {
                return;
            }
        }
    }
}