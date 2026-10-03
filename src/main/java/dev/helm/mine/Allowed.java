package dev.helm.mine;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.minecraft.world.level.block.Block;

import dev.helm.mine.target.TargetFilter;
import dev.helm.mine.target.TargetSelector;
import dev.helm.setting.MiningSettings;
import dev.helm.setting.MovementSettings;

public final class Allowed {

    private Allowed() {
    }

    public static TargetFilter of(TargetFilter wanted, MiningSettings mining,
                                  MovementSettings movement) {
        if (movement.allowBreak()) {
            return wanted;
        }
        Set<Block> still = mining.blocksAllowedToMine();
        List<TargetSelector> kept = new ArrayList<>();
        for (TargetSelector selector : wanted.selectors()) {
            if (still.contains(selector.block())) {
                kept.add(selector);
            }
        }
        return kept.isEmpty() ? null : TargetFilter.of(kept);
    }
}