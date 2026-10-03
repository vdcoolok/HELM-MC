package dev.helm.mine.target;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

@SuppressWarnings({"rawtypes", "unchecked"})
public final class TargetProperty {

    private final Property property;
    private final Comparable value;

    private TargetProperty(Property property, Comparable value) {
        this.property = property;
        this.value = value;
    }

    public static TargetProperty of(Property property, Comparable value) {
        return new TargetProperty(property, value);
    }

    public String name() {
        return property.getName();
    }

    public String written() {
        return property.getName(value);
    }

    public boolean accepts(BlockState state) {
        return value.equals((Comparable) state.getOptionalValue(property).orElse(null));
    }
}