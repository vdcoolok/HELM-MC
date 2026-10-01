package dev.helm.movement.step;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import dev.helm.aim.Aim;
import dev.helm.aim.BlockReach;
import dev.helm.control.Control;
import dev.helm.movement.MoveState;
import dev.helm.movement.MoveTick;
import dev.helm.pathfinding.world.block.BlockShapes;
import dev.helm.pathfinding.world.block.Climbable;
import dev.helm.pathfinding.world.block.Hazards;
import dev.helm.pathfinding.world.block.LiquidRules;

public final class FlatStepExecutor implements StepExecutor {

    private static final double WALK_CLOSE = 0.83D;
    private static final double PLACE_RANGE = 0.6D;
    private static final double BACK_FACE_RANGE = 0.29D;
    private static final double SOUL_SAND_RANGE = 0.85D;
    private static final double PLACEMENT_PITCH = 26.0D;

    private boolean bridgeWasAlwaysThere = true;

    @Override
    public void begin() {
        bridgeWasAlwaysThere = true;
    }

    @Override
    public boolean safeToCancel(StepContext context, PlanStep step, MoveTick tick) {
        return tick.state() != MoveState.RUNNING
                || context.walk().onTop(step.toX(), step.toY() - 1, step.toZ());
    }

    @Override
    public MoveState advance(StepContext context, MoveTick tick, PlanStep step) {
        if (!StepPreparation.ready(context, tick, step)) {
            return MoveState.PREPPING;
        }
        StepPreparation.advanceStatus(tick);
        if (tick.state() != MoveState.RUNNING) {
            return earlyWalk(context, tick, step);
        }

        BlockState belowSource = context.world().stateAt(step.fromX(), step.fromY() - 1, step.fromZ());
        boolean climbing = Climbable.is(belowSource.getBlock());
        tick.set(Control.SNEAK, context.movement().magmaWalkAllowed() && standingOnMagma(context, step));

        BlockState first = firstBlock(context, step);
        BlockState second = secondBlock(context, step);
        if (isDoor(first) || isDoor(second)) {
            if (doorBlocksPath(context, step, first, second)) {
                BlockPos blocked = context.world().stateAt(step.toX(), step.toY(), step.toZ())
                        .getBlock() instanceof DoorBlock
                        ? new BlockPos(step.toX(), step.toY(), step.toZ())
                        : new BlockPos(step.toX(), step.toY() + 1, step.toZ());
                Aim aim = BlockReach.towards(context.player(), blocked, context.look(), false);
                if (aim != null) {
                    tick.intent().aimedAt(aim, true);
                    tick.press(Control.USE);
                    return MoveState.RUNNING;
                }
            }
        }
        if (first.getBlock() instanceof FenceGateBlock || second.getBlock() instanceof FenceGateBlock) {
            Aim aim = gateAim(context, step, first, second);
            if (aim != null) {
                tick.intent().aimedAt(aim, true);
                tick.press(Control.USE);
                return MoveState.RUNNING;
            }
        }

        int[] feet = context.feet();
        boolean bridgePresent = context.walk().onTop(step.toX(), step.toY() - 1, step.toZ())
                || climbing
                || frostWalker(context, step.toX(), step.toY() - 1, step.toZ());
        if (feet[1] != step.toY() && !climbing) {
            if (feet[1] < step.toY()) {
                tick.press(Control.JUMP);
            }
            return MoveState.RUNNING;
        }
        if (bridgePresent) {
            return onBridge(context, tick, step, climbing);
        }
        return onGap(context, tick, step);
    }

    private MoveState earlyWalk(StepContext context, MoveTick tick, PlanStep step) {
        if (!context.movement().walkWhileBreaking() || tick.state() != MoveState.PREPPING) {
            return tick.state();
        }
        BlockState first = firstBlock(context, step);
        BlockState second = secondBlock(context, step);
        if (Hazards.avoidWalkingInto(first, context.movement().magmaWalkAllowed())
                || Hazards.avoidWalkingInto(second, context.movement().magmaWalkAllowed())) {
            return tick.state();
        }
        double distance = flatDistance(context, step.toX(), step.toZ());
        if (distance < WALK_CLOSE || !tick.intent().hasAim()) {
            return tick.state();
        }
        double yawToDest = StepPreparation.centeredOn(context,
                new BlockPos(step.toX(), step.toY(), step.toZ())).yaw();
        double pitch = first.getBlock() instanceof AirBlock
                && (second.getBlock() instanceof AirBlock || BlockShapes.fullCube(second))
                ? PLACEMENT_PITCH
                : tick.intent().aim().pitch();
        tick.intent().aimedAt(new Aim(yawToDest, pitch), true);
        tick.press(Control.MOVE_FORWARD);
        tick.press(Control.SPRINT);
        return tick.state();
    }

    private MoveState onBridge(StepContext context, MoveTick tick, PlanStep step, boolean climbing) {
        bridgeWasAlwaysThere = true;
        int[] feet = context.feet();
        if (feet[0] == step.toX() && feet[1] == step.toY() && feet[2] == step.toZ()) {
            return MoveState.SUCCESS;
        }
        if (context.movement().overshootTraverse() && overshot(context, step)) {
            return MoveState.SUCCESS;
        }
        BlockState low = context.world().stateAt(step.fromX(), step.fromY(), step.fromZ());
        BlockState high = context.world().stateAt(step.fromX(), step.fromY() + 1, step.fromZ());
        if (context.player().getY() > step.fromY() + 0.1D && !context.player().onGround()
                && (Climbable.is(low.getBlock()) || Climbable.is(high.getBlock()))) {
            return MoveState.RUNNING;
        }
        int intoX = step.toX() + step.directionX();
        int intoZ = step.toZ() + step.directionZ();
        BlockState intoBelow = context.world().stateAt(intoX, step.toY() - 1, intoZ);
        BlockState intoAbove = context.world().stateAt(intoX, step.toY(), intoZ);
        boolean steppingInto = context.walk().through(intoX, step.toY(), intoZ);
        if (bridgeWasAlwaysThere
                && (!LiquidRules.any(context.world().stateAt(feet[0], feet[1], feet[2]))
                    || context.movement().sprintInWater())
                && (!Hazards.avoidWalkingInto(intoBelow, context.movement().magmaWalkAllowed())
                    || LiquidRules.water(intoBelow))
                && !Hazards.avoidWalkingInto(intoAbove, context.movement().magmaWalkAllowed())) {
            tick.press(Control.SPRINT);
        }
        if (feet[1] != step.toY() && climbing
                && Climbable.is(context.world().stateAt(step.toX(), step.toY() - 1, step.toZ()).getBlock())) {
            tick.press(Control.JUMP);
        }
        int[] first = step.blocksToBreak().isEmpty()
                ? new int[]{step.toX(), step.toY(), step.toZ()}
                : step.blocksToBreak().get(0);
        StepPreparation.walkTowards(context, tick, first[0], first[1], first[2]);
        return MoveState.RUNNING;
    }

    private boolean overshot(StepContext context, PlanStep step) {
        int[] feet = context.feet();
        int aheadX = step.toX() + step.directionX();
        int aheadZ = step.toZ() + step.directionZ();
        if (feet[0] == aheadX && feet[1] == step.toY() && feet[2] == aheadZ) {
            return true;
        }
        return feet[0] == aheadX + step.directionX()
                && feet[1] == step.toY()
                && feet[2] == aheadZ + step.directionZ();
    }

    private MoveState onGap(StepContext context, MoveTick tick, PlanStep step) {
        bridgeWasAlwaysThere = false;
        int[] feet = context.feet();
        BlockState standingOn = context.world().stateAt(feet[0], feet[1] - 1, feet[2]);
        if (standingOn.getBlock() == Blocks.SOUL_SAND || standingOn.getBlock() instanceof SlabBlock) {
            if (flatDistance(context, step.toX(), step.toZ()) < SOUL_SAND_RANGE) {
                StepPreparation.walkTowards(context, tick, step.toX(), step.toY(), step.toZ());
                tick.release(Control.MOVE_FORWARD);
                tick.press(Control.MOVE_BACK);
                return MoveState.RUNNING;
            }
        }
        double distance = flatDistance(context, step.toX(), step.toZ());
        Placement placement = BlockPlacer.attempt(context, tick, step.toX(), step.toY() - 1, step.toZ(),
                false, true);
        if ((placement == Placement.READY || distance < PLACE_RANGE)
                && !context.movement().assumeSafeWalk()) {
            tick.press(Control.SNEAK);
        }
        switch (placement) {
            case READY -> {
                if (context.player().isCrouching() || context.movement().assumeSafeWalk()) {
                    tick.press(Control.USE);
                }
                return MoveState.RUNNING;
            }
            case ATTEMPTING -> {
                if (distance > WALK_CLOSE) {
                    double yaw = StepPreparation.centeredOn(context,
                            new BlockPos(step.toX(), step.toY(), step.toZ())).yaw();
                    if (tick.intent().hasAim() && Math.abs(tick.intent().aim().yaw() - yaw) < 0.1D) {
                        tick.press(Control.MOVE_FORWARD);
                    }
                } else if (BlockReach.current(context.player()).near(tick.intent().aim())) {
                    tick.press(Control.ATTACK);
                }
                return MoveState.RUNNING;
            }
            default -> {
            }
        }
        if (feet[0] == step.toX() && feet[1] == step.toY() && feet[2] == step.toZ()) {
            return backAndPlace(context, tick, step);
        }
        StepPreparation.walkTowards(context, tick, step.toX(), step.toY(), step.toZ());
        return MoveState.RUNNING;
    }

    private MoveState backAndPlace(StepContext context, MoveTick tick, PlanStep step) {
        double faceX = (step.toX() + step.fromX() + 1.0D) * 0.5D;
        double faceY = (step.toY() + step.fromY() - 1.0D) * 0.5D;
        double faceZ = (step.toZ() + step.fromZ() + 1.0D) * 0.5D;
        BlockPos against = new BlockPos(step.fromX(), step.fromY() - 1, step.fromZ());
        Aim backToFace = dev.helm.aim.Aiming.lookFrom(
                context.player().getX(), context.player().getEyeY(), context.player().getZ(),
                faceX, faceY, faceZ);
        double distance = Math.max(Math.abs(context.player().getX() - faceX),
                Math.abs(context.player().getZ() - faceZ));
        if (distance < BACK_FACE_RANGE) {
            var eyes = BlockReach.eyePosition(context.player(), false);
            double yaw = dev.helm.aim.Aiming.lookFrom(
                    step.toX() + 0.5D, step.toY() + 0.5D, step.toZ() + 0.5D,
                    eyes.x, eyes.y, eyes.z).yaw();
            tick.intent().aimedAt(new Aim(yaw, backToFace.pitch()), true);
            tick.press(Control.MOVE_BACK);
        } else {
            tick.intent().aimedAt(backToFace, true);
        }
        if (lookingAt(context, against)) {
            tick.press(Control.USE);
        } else if (BlockReach.current(context.player()).near(tick.intent().aim())) {
            tick.press(Control.ATTACK);
        }
        return MoveState.RUNNING;
    }

    private boolean doorBlocksPath(StepContext context, PlanStep step,
                                   BlockState first, BlockState second) {
        if (first.getBlock() instanceof DoorBlock
                && !context.walk().through(step.toX(), step.toY(), step.toZ())) {
            return true;
        }
        return second.getBlock() instanceof DoorBlock
                && !context.walk().through(step.toX(), step.toY() + 1, step.toZ());
    }

    private Aim gateAim(StepContext context, PlanStep step, BlockState first, BlockState second) {
        BlockPos lower = new BlockPos(step.toX(), step.toY(), step.toZ());
        BlockPos upper = new BlockPos(step.toX(), step.toY() + 1, step.toZ());
        if (first.getBlock() instanceof FenceGateBlock) {
            return BlockReach.towards(context.player(), lower, context.look(), false);
        }
        if (second.getBlock() instanceof FenceGateBlock) {
            return BlockReach.towards(context.player(), upper, context.look(), false);
        }
        return null;
    }

    private boolean standingOnMagma(StepContext context, PlanStep step) {
        return context.world().stateAt(step.fromX(), step.fromY() - 1, step.fromZ())
                .getBlock() == Blocks.MAGMA_BLOCK;
    }

    private boolean frostWalker(StepContext context, int x, int y, int z) {
        if (context.player() == null || !dev.helm.movement.FrostWalker.wornBy(context.player())) {
            return false;
        }
        var state = context.world().stateAt(x, y, z);
        return state == net.minecraft.world.level.block.IceBlock.meltsInto()
                && state.getValue(net.minecraft.world.level.block.LiquidBlock.LEVEL) == 0;
    }

    private double flatDistance(StepContext context, int x, int z) {
        return Math.max(Math.abs(context.player().getX() - (x + 0.5D)),
                Math.abs(context.player().getZ() - (z + 0.5D)));
    }

    private boolean lookingAt(StepContext context, BlockPos pos) {
        var player = context.player();
        var trace = dev.helm.aim.AimTrace.towards(player,
                new dev.helm.aim.Aim(player.getYRot(), player.getXRot()),
                context.look().blockReachDistance(), player.isCrouching());
        return trace != null
                && trace.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK
                && ((net.minecraft.world.phys.BlockHitResult) trace).getBlockPos().equals(pos);
    }

    private boolean isDoor(BlockState state) {
        return state.getBlock() instanceof DoorBlock;
    }

    private BlockState firstBlock(StepContext context, PlanStep step) {
        return step.blocksToBreak().isEmpty()
                ? context.world().stateAt(step.toX(), step.toY(), step.toZ())
                : blockAt(context, step.blocksToBreak().get(0));
    }

    private BlockState secondBlock(StepContext context, PlanStep step) {
        return step.blocksToBreak().size() < 2
                ? context.world().stateAt(step.toX(), step.toY() + 1, step.toZ())
                : blockAt(context, step.blocksToBreak().get(1));
    }

    private BlockState blockAt(StepContext context, int[] position) {
        return context.world().stateAt(position[0], position[1], position[2]);
    }
}
