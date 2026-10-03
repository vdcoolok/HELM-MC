package dev.helm.mine.target;

import java.util.List;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public record TargetSelector(Block block, List<TargetProperty> properties) {

    public TargetSelector {
        properties = List.copyOf(properties);
    }

    public static TargetSelector whole(Block block) {
        return new TargetSelector(block, List.of());
    }

    public boolean wants(BlockState state) {
        if (state.getBlock() != block) {
            return false;
        }
        for (TargetProperty property : properties) {
            if (!property.accepts(state)) {
                return false;
            }
        }
        return true;
    }

    public String identifier() {
        return dev.helm.world.cache.BlockNames.of(block);
    }

    public String written() {
        if (properties.isEmpty()) {
            return identifier();
        }
        StringBuilder text = new StringBuilder(identifier()).append('[');
        for (int index = 0; index < properties.size(); index++) {
            if (index > 0) {
                text.append(',');
            }
            TargetProperty property = properties.get(index);
            text.append(property.name()).append('=').append(property.written());
        }
        return text.append(']').toString();
    }
}