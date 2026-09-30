package dev.helm.pathfinding.move.expand;

import dev.helm.pathfinding.move.MoveEnvironment;
import dev.helm.pathfinding.move.MoveExpander;
import dev.helm.pathfinding.move.MoveTarget;
import dev.helm.pathfinding.world.block.Climbable;
import dev.helm.pathfinding.world.block.Hazards;
import dev.helm.pathfinding.world.block.LiquidRules;
import dev.helm.pathfinding.cost.MoveCosts;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class StepExpander implements MoveExpander {

    private final MoveEnvironment env;

    public StepExpander(MoveEnvironment env) {
        this.env = env;
    }

    @Override
    public MoveTarget expand(int x, int y, int z, MoveTarget out) {
        int toX = out.x;
        int toZ = out.z;
        BlockState aboveDest = env.stateAt(toX, y + 1, toZ);
        BlockState bodyDest = env.stateAt(toX, y, toZ);
        BlockState underDest = env.stateAt(toX, y - 1, toZ);
        BlockState underSrc = env.stateAt(x, y - 1, z);

        boolean standingOnSolid = env.walk().solidNeededToStand(x, y - 1, z, underSrc);
        boolean frost = standingOnSolid && !env.tuning().assumeWalkOnWater()
                && env.walk().frostWalkerTurns(underDest);

        if (frost || env.walk().onTop(toX, y - 1, toZ, underDest)) {
            out.cost = walkCost(x, y, z, toX, toZ, aboveDest, bodyDest, underDest, underSrc,
                    standingOnSolid, frost);
        } else {
            out.cost = bridgeCost(x, y, z, toX, toZ, aboveDest, bodyDest, underDest, underSrc,
                    standingOnSolid);
        }
        return out;
    }

    private double walkCost(int x, int y, int z, int toX, int toZ, BlockState aboveDest,
                            BlockState bodyDest, BlockState underDest, BlockState underSrc,
                            boolean standingOnSolid, boolean frost) {
        double step = MoveCosts.WALK_ONE;
        boolean water = false;
        boolean sneaking = false;
        if (LiquidRules.water(aboveDest) || LiquidRules.water(bodyDest)) {
            step = env.tuning().waterWalkSpeed();
            water = true;
        } else {
            if (underDest.getBlock() == Blocks.SOUL_SAND) {
                step += soulSandDelta() / 2;
            } else if (underDest.getBlock() == Blocks.WATER) {
                step += env.tuning().waterWalkPenalty();
            }
            if (underSrc.getBlock() == Blocks.SOUL_SAND) {
                step += soulSandDelta() / 2;
            } else if (env.tuning().magmaWalkAllowed()
                    && underSrc.getBlock() == Blocks.MAGMA_BLOCK) {
                sneaking = true;
                step += (MoveCosts.SNEAK_ONE - MoveCosts.WALK_ONE) / 2;
            }
        }
        double body = env.work().breakTicks(toX, y, toZ, bodyDest, false);
        if (body >= MoveCosts.IMPOSSIBLE) {
            return MoveCosts.IMPOSSIBLE;
        }
        double head = env.work().breakTicks(toX, y + 1, toZ, aboveDest, true);
        if (body == 0 && head == 0) {
            if (!water && !sneaking && env.tuning().canSprint()) {
                step *= MoveCosts.SPRINT_MULTIPLIER;
            }
            return step;
        }
        if (Climbable.is(underSrc.getBlock())) {
            body *= 5;
            head *= 5;
        }
        return step + body + head;
    }

    private double bridgeCost(int x, int y, int z, int toX, int toZ, BlockState aboveDest,
                              BlockState bodyDest, BlockState underDest, BlockState underSrc,
                              boolean standingOnSolid) {
        if (Climbable.is(underSrc.getBlock())) {
            return MoveCosts.IMPOSSIBLE;
        }
        if (!env.walk().replaceable(toX, y - 1, toZ, underDest)) {
            return MoveCosts.IMPOSSIBLE;
        }
        boolean throughWater = LiquidRules.water(aboveDest) || LiquidRules.water(bodyDest);
        if (LiquidRules.water(underDest) && throughWater) {
            return MoveCosts.IMPOSSIBLE;
        }
        double place = env.work().placeAt(toX, y - 1, toZ, underDest);
        if (place >= MoveCosts.IMPOSSIBLE) {
            return MoveCosts.IMPOSSIBLE;
        }
        double body = env.work().breakTicks(toX, y, toZ, bodyDest, false);
        if (body >= MoveCosts.IMPOSSIBLE) {
            return MoveCosts.IMPOSSIBLE;
        }
        double head = env.work().breakTicks(toX, y + 1, toZ, aboveDest, true);
        double step = throughWater ? env.tuning().waterWalkSpeed() : MoveCosts.WALK_ONE;

        for (int[] side : dev.helm.pathfinding.move.expand.Sides.AGAINST_ALL_BUT_UP) {
            if (side[0] == x && side[1] == z) {
                continue;
            }
            if (placeableAt(toX + side[0], y - 1 + side[1], toZ + side[2])) {
                return step + place + body + head;
            }
        }
        if (underSrc.getBlock() == Blocks.SOUL_SAND
                || dev.helm.pathfinding.world.block.BlockShapes.halfSlab(underSrc)) {
            return MoveCosts.IMPOSSIBLE;
        }
        if (!standingOnSolid) {
            return MoveCosts.IMPOSSIBLE;
        }
        Block blockSrc = env.stateAt(x, y, z).getBlock();
        if ((blockSrc == Blocks.LILY_PAD
                || blockSrc instanceof net.minecraft.world.level.block.CarpetBlock)
                && !underSrc.getFluidState().isEmpty()) {
            return MoveCosts.IMPOSSIBLE;
        }
        return step * (MoveCosts.SNEAK_ONE / MoveCosts.WALK_ONE) + place + body + head;
    }

    private boolean placeableAt(int x, int y, int z) {
        if (!env.insideBuildHeight(y) || !env.entirelyInsideBorder(x, z)) {
            return false;
        }
        return dev.helm.pathfinding.world.block.Passability
                .placeableAgainst(env.stateAt(x, y, z));
    }

    private static double soulSandDelta() {
        return MoveCosts.WALK_ONE_OVER_SOUL_SAND - MoveCosts.WALK_ONE;
    }
}