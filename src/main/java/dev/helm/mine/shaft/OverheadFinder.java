package dev.helm.mine.shaft;

import java.util.List;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.AirBlock;

import dev.helm.pathfinding.world.BlockView;
import dev.helm.pathfinding.world.block.WorkCosts;

public final class OverheadFinder {

    private OverheadFinder() {
    }

    public static OverheadSpot find(List<BlockPos> known, BlockView world, WorkCosts work,
                                    LocalPlayer player) {
        if (!player.onGround()) {
            return null;
        }
        BlockPos feet = player.blockPosition();
        BlockPos eye = feet.above();
        OverheadSpot best = null;
        double bestDistance = Double.MAX_VALUE;
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
            if (work.avoidBreaking(pos.getX(), pos.getY(), pos.getZ(),
                    world.stateAt(pos.getX(), pos.getY(), pos.getZ()))) {
                continue;
            }
            double distance = pos.distSqr(eye);
            if (distance >= bestDistance) {
                continue;
            }
            bestDistance = distance;
            best = new OverheadSpot(pos, feet.getX(), feet.getY(), feet.getZ());
        }
        return best;
    }
}