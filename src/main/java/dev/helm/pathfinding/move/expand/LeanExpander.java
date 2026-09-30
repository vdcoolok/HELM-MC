package dev.helm.pathfinding.move.expand;

import dev.helm.pathfinding.cost.FallCosts;
import dev.helm.pathfinding.cost.MoveCosts;
import dev.helm.pathfinding.move.MoveEnvironment;
import dev.helm.pathfinding.move.MoveExpander;
import dev.helm.pathfinding.move.MoveTarget;
import dev.helm.pathfinding.world.block.BlockShapes;
import dev.helm.pathfinding.world.block.Climbable;
import dev.helm.pathfinding.world.block.Hazards;
import dev.helm.pathfinding.world.block.LiquidRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class LeanExpander implements MoveExpander {

    private static final double ROOT_TWO = Math.sqrt(2);
    private static final double DIAGONAL_PENALTY = ROOT_TWO - 0.001;

    private final MoveEnvironment env;

    public LeanExpander(MoveEnvironment env) {
        this.env = env;
    }

    @Override
    public MoveTarget expand(int x, int y, int z, MoveTarget out) {
        int toX = out.x;
        int toZ = out.z;

        if (!env.walk().through(toX, y + 1, toZ)) {
            out.cost = MoveCosts.IMPOSSIBLE;
            return out;
        }
        BlockState destBody = env.stateAt(toX, y, toZ);
        BlockState underSrc;
        BlockState landing;
        boolean rising = false;
        boolean falling = false;
        boolean frost = false;

        if (!env.walk().through(toX, y, toZ, destBody)) {
            rising = true;
            if (!env.tuning().diagonalAscendAllowed()
                    || !env.walk().through(x, y + 2, z)
                    || !env.walk().onTop(toX, y, toZ, destBody)
                    || !env.walk().through(toX, y + 2, toZ)) {
                out.cost = MoveCosts.IMPOSSIBLE;
                return out;
            }
            landing = destBody;
            underSrc = env.stateAt(x, y - 1, z);
        } else {
            landing = env.stateAt(toX, y - 1, toZ);
            underSrc = env.stateAt(x, y - 1, z);
            boolean standingOnSolid = env.walk().solidNeededToStand(x, y - 1, z, underSrc);
            frost = standingOnSolid && env.walk().frostWalkerTurns(landing);
            if (!frost && !env.walk().onTop(toX, y - 1, toZ, landing)) {
                falling = true;
                if (!env.tuning().diagonalDescendAllowed()
                        || !env.walk().onTop(toX, y - 2, toZ)
                        || !env.walk().through(toX, y - 1, toZ, landing)) {
                    out.cost = MoveCosts.IMPOSSIBLE;
                    return out;
                }
            }
            frost = frost && !env.tuning().assumeWalkOnWater();
        }

        double step = MoveCosts.WALK_ONE;
        boolean sneaking = false;
        if (landing.getBlock() == Blocks.SOUL_SAND) {
            step += soulSandDelta() / 2;
        } else if (env.tuning().magmaWalkAllowed() && landing.getBlock() == Blocks.MAGMA_BLOCK) {
            step += (MoveCosts.SNEAK_ONE - MoveCosts.WALK_ONE) / 2;
            sneaking = true;
        } else if (landing.getBlock() == Blocks.WATER) {
            step += env.tuning().waterWalkPenalty() * ROOT_TWO;
        }

        Block underSrcBlock = underSrc.getBlock();
        if (Climbable.is(underSrcBlock)) {
            out.cost = MoveCosts.IMPOSSIBLE;
            return out;
        }
        if (underSrcBlock == Blocks.SOUL_SAND) {
            step += soulSandDelta() / 2;
        } else if (env.tuning().magmaWalkAllowed() && underSrcBlock == Blocks.MAGMA_BLOCK) {
            step += (MoveCosts.SNEAK_ONE - MoveCosts.WALK_ONE) / 2;
            sneaking = true;
        }

        BlockState cutA = env.stateAt(x, y - 1, toZ);
        if ((!env.tuning().magmaWalkAllowed() && cutA.getBlock() == Blocks.MAGMA_BLOCK)
                || LiquidRules.lava(cutA)) {
            out.cost = MoveCosts.IMPOSSIBLE;
            return out;
        }
        BlockState cutB = env.stateAt(toX, y - 1, z);
        if (LiquidRules.lava(cutB)) {
            out.cost = MoveCosts.IMPOSSIBLE;
            return out;
        }

        boolean water = false;
        BlockState startState = env.stateAt(x, y, z);
        if (LiquidRules.water(startState) || LiquidRules.water(destBody)) {
            if (rising) {
                out.cost = MoveCosts.IMPOSSIBLE;
                return out;
            }
            step = env.tuning().waterWalkSpeed();
            water = true;
        }

        BlockState sideA = env.stateAt(x, y, toZ);
        BlockState sideB = env.stateAt(toX, y, z);

        if (rising) {
            if (!env.walk().through(x, y + 2, toZ)) {
                out.cost = MoveCosts.IMPOSSIBLE;
                return out;
            }
            boolean clearA = env.walk().through(x, y + 1, toZ)
                    && env.walk().through(x, y, toZ, sideA);
            boolean clearB = env.walk().through(toX, y + 2, z)
                    && env.walk().through(toX, y + 1, z)
                    && env.walk().through(toX, y, z, sideB);
            boolean couldStepUpA = clearA && env.walk().onTop(x, y, toZ, sideA);
            boolean couldStepUpB = clearB && env.walk().onTop(toX, y, z, sideB);
            if ((!clearA && !clearB)
                    || Hazards.avoidWalkingInto(sideA, env.tuning().magmaWalkAllowed())
                    || Hazards.avoidWalkingInto(sideB, env.tuning().magmaWalkAllowed())
                    || couldStepUpA || couldStepUpB) {
                out.cost = MoveCosts.IMPOSSIBLE;
                return out;
            }
            out.cost = step * ROOT_TWO + FallCosts.jumpOneBlock();
            out.y = y + 1;
            return out;
        }

        double breakA = env.work().breakTicks(x, y, toZ, sideA, false);
        double breakB = env.work().breakTicks(toX, y, z, sideB, false);
        if (breakA != 0 && breakB != 0) {
            out.cost = MoveCosts.IMPOSSIBLE;
            return out;
        }
        BlockState sideAHead = env.stateAt(x, y + 1, toZ);
        breakA += env.work().breakTicks(x, y + 1, toZ, sideAHead, true);
        if (breakA != 0 && breakB != 0) {
            out.cost = MoveCosts.IMPOSSIBLE;
            return out;
        }
        BlockState sideBHead = env.stateAt(toX, y + 1, z);
        if (breakA == 0 && ((Hazards.avoidWalkingInto(sideB, env.tuning().magmaWalkAllowed())
                && sideB.getBlock() != Blocks.WATER)
                || Hazards.avoidWalkingInto(sideBHead, env.tuning().magmaWalkAllowed()))) {
            out.cost = MoveCosts.IMPOSSIBLE;
            return out;
        }
        breakB += env.work().breakTicks(toX, y + 1, z, sideBHead, true);
        if (breakA != 0 && breakB != 0) {
            out.cost = MoveCosts.IMPOSSIBLE;
            return out;
        }
        if (breakB == 0 && ((Hazards.avoidWalkingInto(sideA, env.tuning().magmaWalkAllowed())
                && sideA.getBlock() != Blocks.WATER)
                || Hazards.avoidWalkingInto(sideAHead, env.tuning().magmaWalkAllowed()))) {
            out.cost = MoveCosts.IMPOSSIBLE;
            return out;
        }
        if (breakA != 0 || breakB != 0) {
            step *= DIAGONAL_PENALTY;
            if (Climbable.is(startState.getBlock())) {
                out.cost = MoveCosts.IMPOSSIBLE;
                return out;
            }
        } else if (env.tuning().canSprint() && !water && !sneaking) {
            step *= MoveCosts.SPRINT_MULTIPLIER;
        }

        out.cost = step * ROOT_TWO;
        if (falling) {
            out.cost += Math.max(FallCosts.forDistance(1), MoveCosts.CENTER_AFTER_FALL);
            out.y = y - 1;
        } else {
            out.y = y;
        }
        return out;
    }

    private static double soulSandDelta() {
        return MoveCosts.WALK_ONE_OVER_SOUL_SAND - MoveCosts.WALK_ONE;
    }
}