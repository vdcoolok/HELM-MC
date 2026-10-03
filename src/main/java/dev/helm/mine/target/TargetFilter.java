package dev.helm.mine.target;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class TargetFilter {

    private final List<TargetSelector> selectors;
    private final Set<Block> blocks;
    private final Set<Item> items;

    private TargetFilter(List<TargetSelector> selectors, Set<Block> blocks, Set<Item> items) {
        this.selectors = selectors;
        this.blocks = blocks;
        this.items = items;
    }

    public static TargetFilter of(List<TargetSelector> selectors) {
        Set<Block> blocks = new LinkedHashSet<>();
        Set<Item> items = new LinkedHashSet<>();
        for (TargetSelector selector : selectors) {
            blocks.add(selector.block());
            items.add(selector.block().asItem());
        }
        return new TargetFilter(List.copyOf(selectors), Set.copyOf(blocks), Set.copyOf(items));
    }

    public List<TargetSelector> selectors() {
        return selectors;
    }

    public Set<Block> blocks() {
        return blocks;
    }

    public Set<Item> items() {
        return items;
    }

    public boolean wants(Block block) {
        return blocks.contains(block);
    }

    public boolean wants(BlockState state) {
        for (TargetSelector selector : selectors) {
            if (selector.wants(state)) {
                return true;
            }
        }
        return false;
    }

    public String describe() {
        StringBuilder text = new StringBuilder();
        for (TargetSelector selector : selectors) {
            if (text.length() > 0) {
                text.append(", ");
            }
            text.append(selector.written());
        }
        return text.toString();
    }

    @Override
    public String toString() {
        return describe();
    }
}