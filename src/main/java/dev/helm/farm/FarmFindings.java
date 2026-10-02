package dev.helm.farm;

import java.util.List;

import net.minecraft.core.BlockPos;

public record FarmFindings(List<BlockPos> harvestable, List<BlockPos> bareFarmland,
                           List<BlockPos> bareSoulSand, List<BlockPos> bareLogs,
                           List<BlockPos> wantsBoneMeal) {
}