package dev.helm.pathfinding.move.expand;

import dev.helm.pathfinding.cost.FallCosts;
import dev.helm.pathfinding.cost.MoveCosts;
import dev.helm.pathfinding.move.MoveEnvironment;
import dev.helm.pathfinding.move.MoveExpander;
import dev.helm.pathfinding.move.MoveTarget;
import dev.helm.pathfinding.world.block.BlockShapes;
import dev.helm.pathfinding.world.block.Climbable;
import dev.helm.pathfinding.world.block.LiquidRules;
import dev.helm.pathfinding.world.block.Passability;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class StepUpExpander implements MoveExpander {

    private final MoveEnvironment env;

    public StepUpExpander(MoveEnvironment env) {
        this.env = env;
    }

    @Override
    public MoveTarget expand(int x, int y, int z, MoveTarget out) {
        int toX = out.x;
        int toZ = out.z;
        BlockState landing = env.stateAt(toX, y, toZ);

        double placement = 0;
        if (!env.walk().onTop(toX, y, toZ, landing)) {
            placement = env.work().placeAt(toX, y, toZ, landing);
            if (placement >= MoveCosts.IMPOSSIBLE) {
                out.cost = MoveCosts.IMPOSSIBLE;
                return out;
            }
            if (!env.walk().replaceable(toX, y, toZ, landing)) {
                out.cost = MoveCosts.IMPOSSIBLE;
                return out;
            }
            if (!hasPlacementSide(x, y, z, toX, toZ)) {
                out.cost = MoveCosts.IMPOSSIBLE;
                return out;
            }
        }

        BlockState twoAboveSrc = env.stateAt(x, y + 2, z);
        if (env.stateAt(x, y + 3, z).getBlock() instanceof FallingBlock
                && (env.walk().through(x, y + 1, z)
                || !(twoAboveSrc.getBlock() instanceof FallingBlock))) {
            out.cost = MoveCosts.IMPOSSIBLE;
            return out;
        }

        BlockState underSrc = env.stateAt(x, y - 1, z);
        if (Climbable.is(underSrc.getBlock())) {
            out.cost = MoveCosts.IMPOSSIBLE;
            return out;
        }

        boolean fromBottomSlab = BlockShapes.bottomSlab(underSrc);
        boolean toBottomSlab = BlockShapes.bottomSlab(landing);
        if (fromBottomSlab && !toBottomSlab) {
            out.cost = MoveCosts.IMPOSSIBLE;
            return out;
        }

        double walk;
        if (toBottomSlab) {
            if (fromBottomSlab) {
                walk = Math.max(FallCosts.jumpOneBlock(), MoveCosts.WALK_ONE)
                        + env.tuning().jumpPenalty();
            } else {
                walk = MoveCosts.WALK_ONE;
            }
        } else if (landing.getBlock() == Blocks.SOUL_SAND) {
            walk = MoveCosts.WALK_ONE_OVER_SOUL_SAND;
        } else if (landing.getBlock() == Blocks.MAGMA_BLOCK) {
            walk = MoveCosts.SNEAK_ONE;
        } else {
            walk = Math.max(FallCosts.jumpOneBlock(), MoveCosts.WALK_ONE);
            walk += env.tuning().jumpPenalty();
        }

        double total = walk + placement;
        total += env.work().breakTicks(x, y + 2, z, twoAboveSrc, false);
        if (total >= MoveCosts.IMPOSSIBLE) {
            out.cost = MoveCosts.IMPOSSIBLE;
            return out;
        }
        total += env.work().breakTicks(toX, y + 1, toZ, false);
        if (total >= MoveCosts.IMPOSSIBLE) {
            out.cost = MoveCosts.IMPOSSIBLE;
            return out;
        }
        total += env.work().breakTicks(toX, y + 2, toZ, true);
        out.cost = total;
        return out;
    }

    private boolean hasPlacementSide(int x, int y, int z, int toX, int toZ) {
        for (int[] side : Sides.AGAINST_ALL_BUT_UP) {
            int againstX = toX + side[0];
            int againstY = y + side[1];
            int againstZ = toZ + side[2];
            if (againstX == x && againstZ == z) {
                continue;
            }
            if (!env.insideBorder(againstX, againstY, againstZ)) {
                continue;
            }
            if (Passability.placeableAgainst(env.stateAt(againstX, againstY, againstZ))) {
                return true;
            }
        }
        return false;
    }
}