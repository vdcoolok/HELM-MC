package dev.helm.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class BlockOutlines {

    private BlockOutlines() {
    }

    public static AABB at(int[] position) {
        Minecraft client = Minecraft.getInstance();
        ClientLevel level = client == null ? null : client.level;
        if (level == null) {
            return null;
        }
        BlockPos pos = new BlockPos(position[0], position[1], position[2]);
        BlockState state = level.getBlockState(pos);
        VoxelShape shape = state.getShape(level, pos);
        if (shape.isEmpty()) {
            shape = Shapes.block();
        }
        return shape.bounds().move(pos);
    }
}
