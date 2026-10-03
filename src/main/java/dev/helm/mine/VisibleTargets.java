package dev.helm.mine;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;

import dev.helm.aim.BlockReach;
import dev.helm.mine.find.TargetPruner;
import dev.helm.pathfinding.world.BlockView;
import dev.helm.pathfinding.world.block.WorkCosts;
import dev.helm.setting.MiningSettings;

public final class VisibleTargets {

    private static final int RADIUS = 10;
    private static final double SIGHT_RANGE = 20.0D;
    private static final int TOUCHING_TARGETS_SQUARED = 2;

    private VisibleTargets() {
    }

    public static void refresh(MineJob job, ClientLevel level, BlockView world, WorkCosts work,
                               MiningSettings settings, LocalPlayer player, BlockPos feet) {
        List<BlockPos> found = new ArrayList<>();
        for (int x = feet.getX() - RADIUS; x <= feet.getX() + RADIUS; x++) {
            for (int y = feet.getY() - RADIUS; y <= feet.getY() + RADIUS; y++) {
                for (int z = feet.getZ() - RADIUS; z <= feet.getZ() + RADIUS; z++) {
                    if (!job.filter().wants(world.stateAt(x, y, z))) {
                        continue;
                    }
                    BlockPos pos = new BlockPos(x, y, z);
                    if (!nearKnown(job, settings, pos) && !inSight(player, pos)) {
                        continue;
                    }
                    found.add(pos);
                }
            }
        }
        List<BlockPos> drops = job.dropsIn(level);
        found.addAll(drops);
        job.known().clear();
        job.known().addAll(TargetPruner.prune(found, job.filter(), world, work, settings, feet,
                job.unreachable(), drops, settings.maxTargets()));
    }

    private static boolean nearKnown(MineJob job, MiningSettings settings, BlockPos pos) {
        if (!settings.sightDiagonals()) {
            return false;
        }
        return job.known().stream()
                .anyMatch(known -> known.distSqr(pos) <= TOUCHING_TARGETS_SQUARED);
    }

    private static boolean inSight(LocalPlayer player, BlockPos pos) {
        return BlockReach.reachableFrom(player, pos, SIGHT_RANGE, player.isCrouching()) != null;
    }
}