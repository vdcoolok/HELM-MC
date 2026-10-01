package dev.helm.pathfinding.move.expand;

import dev.helm.pathfinding.cost.MoveCosts;
import dev.helm.pathfinding.move.MoveEnvironment;
import dev.helm.pathfinding.move.MoveExpander;
import dev.helm.pathfinding.move.MoveTarget;
import dev.helm.pathfinding.world.block.BlockShapes;
import dev.helm.pathfinding.world.block.Climbable;
import dev.helm.pathfinding.world.block.Hazards;
import dev.helm.pathfinding.world.block.LiquidRules;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class BoundExpander implements MoveExpander {

    private static final int[] DIRECTION_X = {0, 0, 1, -1};
    private static final int[] DIRECTION_Z = {-1, 1, 0, 0};

    private final MoveEnvironment env;

    public BoundExpander(MoveEnvironment env) {
        this.env = env;
    }

    @Override
    public MoveTarget expand(int x, int y, int z, MoveTarget out) {
        if (!env.tuning().parkourAllowed()) {
            out.cost = MoveCosts.IMPOSSIBLE;
            return out;
        }
        if (!env.tuning().jumpAtBuildLimit() && y >= env.ceiling() - 1) {
            out.cost = MoveCosts.IMPOSSIBLE;
            return out;
        }
        int dirX = Integer.signum(out.x - x);
        int dirZ = Integer.signum(out.z - z);
        if (!cardinal(dirX, dirZ)) {
            out.cost = MoveCosts.IMPOSSIBLE;
            return out;
        }
        return bound(x, y, z, dirX, dirZ, out);
    }

    private MoveTarget bound(int x, int y, int z, int dirX, int dirZ, MoveTarget out) {
        if (!env.walk().fullyPassable(x + dirX, y, z + dirZ)) {
            out.cost = MoveCosts.IMPOSSIBLE;
            return out;
        }
        BlockState adjacent = env.stateAt(x + dirX, y - 1, z + dirZ);
        if (env.walk().onTop(x + dirX, y - 1, z + dirZ, adjacent)) {
            out.cost = MoveCosts.IMPOSSIBLE;
            return out;
        }
        if (Hazards.avoidWalkingInto(adjacent, env.tuning().magmaWalkAllowed())
                && !LiquidRules.water(adjacent)) {
            out.cost = MoveCosts.IMPOSSIBLE;
            return out;
        }
        if (!env.walk().fullyPassable(x + dirX, y + 1, z + dirZ)
                || !env.walk().fullyPassable(x + dirX, y + 2, z + dirZ)
                || !env.walk().fullyPassable(x, y + 2, z)) {
            out.cost = MoveCosts.IMPOSSIBLE;
            return out;
        }

        BlockState standingOn = env.stateAt(x, y - 1, z);
        if (Climbable.is(standingOn.getBlock())
                || standingOn.getBlock() instanceof StairBlock
                || BlockShapes.bottomSlab(standingOn)) {
            out.cost = MoveCosts.IMPOSSIBLE;
            return out;
        }
        if (env.tuning().assumeWalkOnWater() && !standingOn.getFluidState().isEmpty()) {
            out.cost = MoveCosts.IMPOSSIBLE;
            return out;
        }
        if (!env.stateAt(x, y, z).getFluidState().isEmpty()) {
            out.cost = MoveCosts.IMPOSSIBLE;
            return out;
        }

        int reach;
        if (env.tuning().magmaWalkAllowed() && standingOn.getBlock() == Blocks.MAGMA_BLOCK) {
            reach = 2;
        } else if (standingOn.getBlock() == Blocks.SOUL_SAND) {
            reach = 2;
        } else if (env.tuning().canSprint()) {
            reach = 4;
        } else {
            reach = 3;
        }

        int verified = 1;
        for (int step = 2; step <= reach; step++) {
            int landX = x + dirX * step;
            int landZ = z + dirZ * step;
            if (!env.walk().fullyPassable(landX, y + 1, landZ)
                    || !env.walk().fullyPassable(landX, y + 2, landZ)) {
                break;
            }
            BlockState into = env.stateAt(landX, y, landZ);
            if (!env.walk().fullyPassable(landX, y, landZ, into)) {
                if (step <= 3 && env.tuning().parkourAscendAllowed() && env.tuning().canSprint()
                        && env.walk().onTop(landX, y, landZ, into)
                        && overshootSafe(landX + dirX, y + 1, landZ + dirZ)) {
                    out.x = landX;
                    out.y = y + 1;
                    out.z = landZ;
                    out.cost = step * MoveCosts.SPRINT_ONE + env.tuning().jumpPenalty();
                    return out;
                }
                break;
            }
            BlockState landingOn = env.stateAt(landX, y - 1, landZ);
            boolean solidLanding = landingOn.getBlock() != Blocks.FARMLAND
                    && env.walk().onTop(landX, y - 1, landZ, landingOn);
            boolean frosted = Math.min(16, env.tuning().frostWalkerLevel() + 2) >= step
                    && env.walk().frostWalkerTurns(landingOn);
            if (solidLanding || frosted) {
                if (overshootSafe(landX + dirX, y, landZ + dirZ)) {
                    out.x = landX;
                    out.y = y;
                    out.z = landZ;
                    out.cost = BoundCosts.forJumpDistance(step) + env.tuning().jumpPenalty();
                    return out;
                }
                break;
            }
            if (!env.walk().fullyPassable(landX, y + 3, landZ)) {
                break;
            }
            verified = step;
        }
        return ParkourPlacement.tryFrom(env, x, y, z, dirX, dirZ, verified, out);
    }

    private boolean overshootSafe(int x, int y, int z) {
        boolean here = Hazards.avoidWalkingInto(env.stateAt(x, y, z), env.tuning().magmaWalkAllowed());
        boolean above = Hazards.avoidWalkingInto(env.stateAt(x, y + 1, z),
                env.tuning().magmaWalkAllowed());
        return !here && !above;
    }

    private static boolean cardinal(int dirX, int dirZ) {
        for (int dir = 0; dir < 4; dir++) {
            if (DIRECTION_X[dir] == dirX && DIRECTION_Z[dir] == dirZ) {
                return true;
            }
        }
        return false;
    }
}