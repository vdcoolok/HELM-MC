package dev.helm.pathfinding.world.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public final class Climbable {

    private Climbable() {
    }

    public static boolean is(Block block) {
        return block == Blocks.LADDER
                || block == Blocks.VINE
                || block == Blocks.WEEPING_VINES
                || block == Blocks.WEEPING_VINES_PLANT
                || block == Blocks.TWISTING_VINES
                || block == Blocks.TWISTING_VINES_PLANT;
    }

    public static boolean is(net.minecraft.world.level.block.state.BlockState state) {
        return is(state.getBlock());
    }
}