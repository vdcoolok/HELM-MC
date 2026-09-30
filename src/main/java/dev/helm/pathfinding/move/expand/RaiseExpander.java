package dev.helm.pathfinding.move.expand;

import dev.helm.pathfinding.cost.FallCosts;
import dev.helm.pathfinding.cost.MoveCosts;
import dev.helm.pathfinding.move.MoveEnvironment;
import dev.helm.pathfinding.move.MoveExpander;
import dev.helm.pathfinding.move.MoveTarget;
import dev.helm.pathfinding.world.block.Climbable;
import dev.helm.pathfinding.world.block.LiquidRules;
import dev.helm.pathfinding.world.block.Passability;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;

public final class RaiseExpander implements MoveExpander {

    private final MoveEnvironment env;

    public RaiseExpander(MoveEnvironment env) {
        this.env = env;
    }

    @Override
    public MoveTarget expand(int x, int y, int z, MoveTarget out) {
        BlockState fromState = env.stateAt(x, y, z);
        boolean climbing = Climbable.is(fromState.getBlock());
        BlockState underState = env.stateAt(x, y - 1, z);

        if (!climbing) {
            if (Climbable.is(underState.getBlock())) {
                out.cost = MoveCosts.IMPOSSIBLE;
                return out;
            }
            if (underState.getBlock() instanceof SlabBlock
                    && underState.getValue(SlabBlock.TYPE) == SlabType.BOTTOM) {
                out.cost = MoveCosts.IMPOSSIBLE;
                return out;
            }
        }

        BlockState toBreak = env.stateAt(x, y + 2, z);
        if (toBreak.getBlock() instanceof FenceGateBlock) {
            out.cost = MoveCosts.IMPOSSIBLE;
            return out;
        }

        BlockState oneAboveSrc = null;
        if (LiquidRules.water(toBreak) && LiquidRules.water(fromState)) {
            oneAboveSrc = env.stateAt(x, y + 1, z);
            if (LiquidRules.water(oneAboveSrc)) {
                out.cost = MoveCosts.LADDER_UP_ONE;
                return out;
            }
        }

        double place = 0;
        if (!climbing) {
            place = env.work().placeAt(x, y, z, fromState);
            if (place >= MoveCosts.IMPOSSIBLE) {
                out.cost = MoveCosts.IMPOSSIBLE;
                return out;
            }
            if (underState.getBlock() instanceof AirBlock) {
                place += 0.1;
            }
        }

        if ((LiquidRules.any(fromState) && !Passability.placeableAgainst(underState))
                || (LiquidRules.any(underState) && env.tuning().assumeWalkOnWater())) {
            out.cost = MoveCosts.IMPOSSIBLE;
            return out;
        }
        if ((fromState.getBlock() == Blocks.LILY_PAD
                || fromState.getBlock() instanceof net.minecraft.world.level.block.CarpetBlock)
                && !underState.getFluidState().isEmpty()) {
            out.cost = MoveCosts.IMPOSSIBLE;
            return out;
        }

        double hardness = env.work().breakTicks(x, y + 2, z, toBreak, true);
        if (hardness >= MoveCosts.IMPOSSIBLE) {
            out.cost = MoveCosts.IMPOSSIBLE;
            return out;
        }
        if (hardness != 0) {
            if (Climbable.is(toBreak.getBlock())) {
                hardness = 0;
            } else {
                BlockState threeAbove = env.stateAt(x, y + 3, z);
                if (threeAbove.getBlock() instanceof FallingBlock) {
                    if (oneAboveSrc == null) {
                        oneAboveSrc = env.stateAt(x, y + 1, z);
                    }
                    if (!(toBreak.getBlock() instanceof FallingBlock)
                            || !(oneAboveSrc.getBlock() instanceof FallingBlock)) {
                        out.cost = MoveCosts.IMPOSSIBLE;
                        return out;
                    }
                }
            }
        }

        out.cost = climbing
                ? MoveCosts.LADDER_UP_ONE + hardness * 5
                : FallCosts.jumpOneBlock() + place + env.tuning().jumpPenalty() + hardness;
        return out;
    }
}