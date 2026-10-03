package dev.helm.farm;

import java.util.LinkedHashSet;
import java.util.Set;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import dev.helm.setting.FarmSettings;
import dev.helm.world.read.ChunkScanRequest;
import dev.helm.world.read.Sweep;

public final class FarmScan {

    private static final int CHUNKS_OUT = 10;

    private FarmScan() {
    }

    public static Sweep begin(ClientLevel level, BlockPos from, FarmSettings settings) {
        return Sweep.around(level, from,
                new ChunkScanRequest(watchedFor(settings), settings.maxTargets(),
                CHUNKS_OUT));
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