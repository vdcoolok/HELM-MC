package dev.helm.farm;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.AirBlock;

import dev.helm.drops.DropCache;
import dev.helm.pathfinding.goal.AnyGoal;
import dev.helm.pathfinding.world.block.LiquidRules;
import dev.helm.pathfinding.goal.BesideGoal;
import dev.helm.pathfinding.goal.BlockGoal;
import dev.helm.pathfinding.goal.BreakableGoal;
import dev.helm.pathfinding.goal.DroppedGoal;
import dev.helm.pathfinding.goal.Goal;

public final class FarmGoals {

    private FarmGoals() {
    }

    public static AnyGoal from(FarmFindings found, LocalPlayer player, ClientLevel level) {
        List<Goal> targets = new ArrayList<>();
        for (BlockPos pos : found.harvestable()) {
            targets.add(new BreakableGoal(pos.getX(), pos.getY(), pos.getZ()));
        }
        if (FarmCarrying.carried(player, FarmItems::plantable)) {
            for (BlockPos pos : found.bareFarmland()) {
                targets.add(block(pos.above()));
            }
        }
        if (FarmCarrying.carried(player, FarmItems::netherWart)) {
            for (BlockPos pos : found.bareSoulSand()) {
                targets.add(block(pos.above()));
            }
        }
        if (FarmCarrying.carried(player, FarmItems::cocoaBeans)) {
            for (BlockPos pos : found.bareLogs()) {
                for (Direction side : Direction.Plane.HORIZONTAL) {
                    BlockPos air = pos.relative(side);
                    if (level.getBlockState(air).getBlock() instanceof AirBlock) {
                        targets.add(new BesideGoal(air.getX(), air.getY(), air.getZ()));
                    }
                }
            }
        }
        if (FarmCarrying.carried(player, FarmItems::boneMeal)) {
            for (BlockPos pos : found.wantsBoneMeal()) {
                targets.add(block(pos));
            }
        }
        targets.addAll(droppedWorthCollecting(level));
        return AnyGoal.of(targets);
    }

    private static List<Goal> droppedWorthCollecting(ClientLevel level) {
        DropCache cache = DropCache.instance();
        List<Goal> targets = new ArrayList<>();
        for (Entity entity : level.entitiesForRendering()) {
            if (!(entity instanceof ItemEntity dropped) || dropped.getItem().isEmpty()) {
                continue;
            }
            if (!cache.wanted(dropped.getItem())) {
                continue;
            }
            if (dropped.onGround()) {
                targets.add(new DroppedGoal(where(dropped)));
                continue;
            }
            if (afloat(dropped, level)) {
                BlockPos where = where(dropped);
                targets.add(new BesideGoal(where.getX(), where.getY(), where.getZ()));
            }
        }
        return targets;
    }

    private static boolean afloat(ItemEntity dropped, ClientLevel level) {
        BlockPos pos = dropped.blockPosition();
        return LiquidRules.water(level.getBlockState(pos))
                || LiquidRules.water(level.getBlockState(pos.below()));
    }

    private static BlockPos where(ItemEntity dropped) {
        return BlockPos.containing(dropped.getX(), dropped.getY() + 0.1D, dropped.getZ());
    }

    private static Goal block(BlockPos pos) {
        return new BlockGoal(pos.getX(), pos.getY(), pos.getZ());
    }
}