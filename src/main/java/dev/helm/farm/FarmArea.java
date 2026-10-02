package dev.helm.farm;

import net.minecraft.core.BlockPos;

public record FarmArea(BlockPos centre, int range) {

    public static FarmArea everywhere(BlockPos centre) {
        return new FarmArea(centre, 0);
    }

    public boolean covers(BlockPos pos) {
        return range == 0 || pos.distSqr(centre) <= range * range;
    }

    public String describe() {
        return range == 0 ? "everywhere" : range + " blocks";
    }
}