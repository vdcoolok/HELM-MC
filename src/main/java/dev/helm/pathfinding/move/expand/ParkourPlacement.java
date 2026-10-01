package dev.helm.pathfinding.move.expand;

import dev.helm.pathfinding.cost.MoveCosts;
import dev.helm.pathfinding.move.MoveEnvironment;
import dev.helm.pathfinding.move.MoveTarget;
import dev.helm.pathfinding.world.block.Hazards;
import dev.helm.pathfinding.world.block.Passability;
import net.minecraft.world.level.block.state.BlockState;

public final class ParkourPlacement {

    private ParkourPlacement() {
    }

    public static MoveTarget tryFrom(MoveEnvironment env, int x, int y, int z,
                                     int dirX, int dirZ, int furthest, MoveTarget out) {
        if (!env.tuning().parkourPlaceAllowed()) {
            out.cost = MoveCosts.IMPOSSIBLE;
            return out;
        }
        for (int step = furthest; step > 1; step--) {
            int landX = x + dirX * step;
            int landZ = z + dirZ * step;
            BlockState toReplace = env.stateAt(landX, y - 1, landZ);
            double placeCost = env.work().placeAt(landX, y - 1, landZ, toReplace);
            if (placeCost >= MoveCosts.IMPOSSIBLE) {
                continue;
            }
            if (!env.walk().replaceable(landX, y - 1, landZ, toReplace)) {
                continue;
            }
            if (env.tuning().magmaWalkAllowed()) {
                boolean hazard = Hazards.avoidWalkingInto(env.stateAt(landX + dirX, y, landZ + dirZ),
                        true)
                        || Hazards.avoidWalkingInto(
                                env.stateAt(landX + dirX, y + 1, landZ + dirZ), true);
                if (hazard) {
                    continue;
                }
            }
            if (hasSideToPlaceAgainst(env, landX, y - 1, landZ, dirX, dirZ)) {
                out.x = landX;
                out.y = y;
                out.z = landZ;
                out.cost = BoundCosts.forJumpDistance(step) + placeCost
                        + env.tuning().jumpPenalty();
                return out;
            }
        }
        out.cost = MoveCosts.IMPOSSIBLE;
        return out;
    }

    private static boolean hasSideToPlaceAgainst(MoveEnvironment env, int landX, int landY,
                                                  int landZ, int dirX, int dirZ) {
        for (int[] side : Sides.AGAINST_ALL_BUT_UP) {
            int againstX = landX + side[0];
            int againstY = landY + side[1];
            int againstZ = landZ + side[2];
            if (againstX == landX - dirX && againstZ == landZ - dirZ) {
                continue;
            }
            if (!env.insideBuildHeight(againstY) || !env.entirelyInsideBorder(againstX, againstZ)) {
                continue;
            }
            if (Passability.placeableAgainst(env.stateAt(againstX, againstY, againstZ))) {
                return true;
            }
        }
        return false;
    }
}