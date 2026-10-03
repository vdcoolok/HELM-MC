package dev.helm.mine.shaft;

import java.util.List;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.AirBlock;

import dev.helm.aim.BlockDistance;
import dev.helm.pathfinding.world.BlockView;
import dev.helm.setting.LookSettings;

public final class ColumnRiser {

    private ColumnRiser() {
    }

    public static ColumnRise find(List<BlockPos> known, BlockView world, BlockPos feet,
                                   LocalPlayer player, LookSettings look) {
        BlockPos above = lowestAbove(known, world, feet);
        if (above == null) {
            return null;
        }
        if (BlockDistance.nearest(player, above, player.isCrouching())
                <= look.blockReachDistance()) {
            return null;
        }
        return new ColumnRise(above, feet.getX(), feet.getY() + 1, feet.getZ());
    }

    private static BlockPos lowestAbove(List<BlockPos> known, BlockView world, BlockPos feet) {
        BlockPos lowest = null;
        for (BlockPos pos : known) {
            if (pos.getX() != feet.getX() || pos.getZ() != feet.getZ()) {
                continue;
            }
            if (pos.getY() < feet.getY()) {
                continue;
            }
            if (world.stateAt(pos.getX(), pos.getY(), pos.getZ()).getBlock() instanceof AirBlock) {
                continue;
            }
            if (lowest == null || pos.getY() < lowest.getY()) {
                lowest = pos;
            }
        }
        return lowest;
    }
}