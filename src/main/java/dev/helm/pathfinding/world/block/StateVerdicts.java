package dev.helm.pathfinding.world.block;

import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.AzaleaBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StainedGlassBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;

public final class StateVerdicts {

    private static final int UNKNOWN = 0;

    private static final int NO = 1;
    private static final int MAYBE = 2;
    private static final int YES = 3;

    private static final int THROUGH_SHIFT = 0;
    private static final int ON_TOP_SHIFT = 2;
    private static final int PASSABLE_SHIFT = 4;

    private final byte[] verdicts;

    public StateVerdicts() {
        this.verdicts = new byte[Block.BLOCK_STATE_REGISTRY.size()];
    }

    public Ternary through(BlockState state) {
        return read(state, THROUGH_SHIFT);
    }

    public Ternary onTop(BlockState state) {
        return read(state, ON_TOP_SHIFT);
    }

    public Ternary fullyPassable(BlockState state) {
        return read(state, PASSABLE_SHIFT);
    }

    private Ternary read(BlockState state, int shift) {
        int id = Block.BLOCK_STATE_REGISTRY.getId(state);
        int packed = verdicts[id] & 0xFF;
        if (packed == UNKNOWN) {
            packed = classify(state);
            verdicts[id] = (byte) packed;
        }
        return toTernary((packed >> shift) & 3);
    }

    private static Ternary toTernary(int value) {
        if (value == NO) {
            return Ternary.NO;
        }
        return value == MAYBE ? Ternary.MAYBE : Ternary.YES;
    }

    private static int classify(BlockState state) {
        return encode(throughOf(state), THROUGH_SHIFT)
                | encode(onTopOf(state), ON_TOP_SHIFT)
                | encode(passableOf(state), PASSABLE_SHIFT);
    }

    private static int encode(Ternary verdict, int shift) {
        int value = switch (verdict) {
            case NO -> NO;
            case MAYBE -> MAYBE;
            case YES -> YES;
        };
        return value << shift;
    }

    private static Ternary throughOf(BlockState state) {
        Block block = state.getBlock();
        if (block instanceof AirBlock) {
            return Ternary.YES;
        }
        if (Passability.neverWalk(block)) {
            return Ternary.NO;
        }
        if (Passability.alwaysWalk(state)) {
            return Ternary.YES;
        }
        if (block == Blocks.IRON_DOOR) {
            return Ternary.NO;
        }
        if (Passability.maybeWalk(state)) {
            return Ternary.MAYBE;
        }
        if (LiquidRules.any(state)) {
            return state.getFluidState().getType().getAmount(state.getFluidState()) == 8
                    ? Ternary.MAYBE
                    : Ternary.NO;
        }
        return BlockShapes.landBound(state) ? Ternary.YES : Ternary.NO;
    }

    private static Ternary onTopOf(BlockState state) {
        Block block = state.getBlock();
        if (BlockShapes.fullCube(state)) {
            if (block == Blocks.MAGMA_BLOCK) {
                return Ternary.MAYBE;
            }
            if (block == Blocks.BUBBLE_COLUMN || block == Blocks.HONEY_BLOCK) {
                return Ternary.NO;
            }
            return Ternary.YES;
        }
        if (block == Blocks.LADDER
                || block == Blocks.FARMLAND
                || block == Blocks.DIRT_PATH
                || block == Blocks.SOUL_SAND
                || block == Blocks.ENDER_CHEST
                || block == Blocks.CHEST
                || block == Blocks.TRAPPED_CHEST
                || block == Blocks.GLASS
                || block instanceof StainedGlassBlock
                || block instanceof StairBlock
                || block instanceof AzaleaBlock) {
            return Ternary.YES;
        }
        if (Climbable.is(block)
                || block instanceof SlabBlock
                || LiquidRules.any(state)) {
            return Ternary.MAYBE;
        }
        return Ternary.NO;
    }

    private static Ternary passableOf(BlockState state) {
        if (state.getBlock() instanceof AirBlock) {
            return Ternary.YES;
        }
        if (Passability.neverFullyPass(state.getBlock()) || LiquidRules.any(state)) {
            return Ternary.NO;
        }
        return BlockShapes.landBound(state) ? Ternary.YES : Ternary.NO;
    }
}
