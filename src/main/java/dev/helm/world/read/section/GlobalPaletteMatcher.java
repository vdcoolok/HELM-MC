package dev.helm.world.read.section;

import java.util.Set;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class GlobalPaletteMatcher implements SectionMatcher {

    private final Set<Block> wanted;

    public GlobalPaletteMatcher(Set<Block> wanted) {
        this.wanted = wanted;
    }

    @Override
    public boolean accepts(int index) {
        BlockState state = Block.BLOCK_STATE_REGISTRY.byId(index);
        return state != null && wanted.contains(state.getBlock());
    }

    @Override
    public boolean fills() {
        return false;
    }
}
