package dev.helm.movement.step;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import dev.helm.aim.Aim;
import dev.helm.aim.BlockReach;
import dev.helm.control.Control;
import dev.helm.movement.MoveState;
import dev.helm.movement.MoveTick;
import dev.helm.pathfinding.world.block.Passability;
import dev.helm.tools.ThrowawayChooser;

public final class BlockPlacer {

    private static final Direction[] AGAINST = {
            Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.DOWN
    };

    private BlockPlacer() {
    }

    public static Placement attempt(StepContext context, MoveTick tick,
                                    int x, int y, int z, boolean preferDown, boolean wouldSneak) {
        BlockPos placeAt = new BlockPos(x, y, z);
        boolean found = false;

        Aim direct = BlockReach.towards(context.player(), placeAt, context.look(), wouldSneak);
        if (direct != null) {
            tick.intent().aimedAt(direct, true);
            found = true;
        }

        for (Direction side : AGAINST) {
            BlockPos against = placeAt.relative(side);
            if (!Passability.placeableAgainst(context.world().stateAt(
                    against.getX(), against.getY(), against.getZ()))) {
                continue;
            }
            if (!ThrowawayChooser.selectFor(false, x, y, z)) {
                tick.state(MoveState.UNREACHABLE);
                return Placement.NO_OPTION;
            }
            Aim aim = face(context, placeAt, against, wouldSneak);
            Aim actual = dev.helm.aim.LookController.instance()
                    .quantise(context.player(), aim);
            HitResult trace = BlockReach.trace(context.player(), actual, context.look(), wouldSneak);
            if (trace != null && trace.getType() == HitResult.Type.BLOCK
                    && ((BlockHitResult) trace).getBlockPos().equals(against)
                    && ((BlockHitResult) trace).getBlockPos()
                            .relative(((BlockHitResult) trace).getDirection()).equals(placeAt)) {
                tick.intent().aimedAt(aim, true);
                found = true;
                if (!preferDown) {
                    break;
                }
            }
        }

        BlockHitResult aimed = aimedAt(context);
        BlockPos selected = aimed == null ? null : aimed.getBlockPos();
        if (selected != null) {
            Direction side = aimed.getDirection();
            if (selected.equals(placeAt)
                    || (Passability.placeableAgainst(context.world().stateAt(
                            selected.getX(), selected.getY(), selected.getZ()))
                    && selected.relative(side).equals(placeAt))) {
                if (wouldSneak) {
                    tick.press(Control.SNEAK);
                }
                ThrowawayChooser.selectFor(true, x, y, z);
                return Placement.READY;
            }
        }

        if (found) {
            if (wouldSneak) {
                tick.press(Control.SNEAK);
            }
            ThrowawayChooser.selectFor(true, x, y, z);
            return Placement.ATTEMPTING;
        }
        return Placement.NO_OPTION;
    }

    private static Aim face(StepContext context, BlockPos placeAt, BlockPos against, boolean wouldSneak) {
        double faceX = (placeAt.getX() + against.getX() + 1.0D) * 0.5D;
        double faceY = (placeAt.getY() + against.getY() + 0.5D) * 0.5D;
        double faceZ = (placeAt.getZ() + against.getZ() + 1.0D) * 0.5D;
        var eyes = BlockReach.eyePosition(context.player(), wouldSneak);
        return dev.helm.aim.Aiming.lookFrom(eyes.x, eyes.y, eyes.z, faceX, faceY, faceZ);
    }

    private static BlockHitResult aimedAt(StepContext context) {
        var player = context.player();
        HitResult trace = dev.helm.aim.AimTrace.towards(player,
                new Aim(player.getYRot(), player.getXRot()),
                context.look().blockReachDistance(), false);
        if (trace == null || trace.getType() != HitResult.Type.BLOCK) {
            return null;
        }
        return (BlockHitResult) trace;
    }
}
