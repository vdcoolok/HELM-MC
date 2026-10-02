package dev.helm.farm;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import dev.helm.setting.FarmSettings;
import dev.helm.world.read.BlockScan;
import dev.helm.world.read.ChunkScanRequest;

public final class FarmScan {

    private static final int CHUNKS_OUT = 10;
    private static final int LEVELS_AROUND = 10;

    private FarmScan() {
    }

    public static List<BlockPos> sweep(ClientLevel level, BlockPos from, FarmSettings settings) {
        return BlockScan.around(level, from, new ChunkScanRequest(watchedFor(settings),
                settings.maxTargets(), CHUNKS_OUT, LEVELS_AROUND));
    }

    public static Set<Block> watchedFor(FarmSettings settings) {
        Set<Block> wanted = new LinkedHashSet<>(Crops.watched());
        if (settings.replant()) {
            wanted.add(Blocks.FARMLAND);
            wanted.add(Blocks.JUNGLE_LOG);
            if (settings.replantWart()) {
                wanted.add(Blocks.SOUL_SAND);
            }
        }
        return wanted;
    }
}