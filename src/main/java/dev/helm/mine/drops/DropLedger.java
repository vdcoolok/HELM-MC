package dev.helm.mine.drops;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.core.BlockPos;

public final class DropLedger {

    private final Map<BlockPos, Long> waiting = new HashMap<>();

    public void hold(BlockPos pos, long deadline) {
        waiting.put(pos, deadline);
    }

    public void expire(long now) {
        waiting.values().removeIf(deadline -> deadline < now);
    }

    public List<BlockPos> positions() {
        return new ArrayList<>(waiting.keySet());
    }
}