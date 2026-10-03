package dev.helm.follow.standing;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public record Target(Entity entity, EntityType<?> kind, String label, double distance) {

    public static Target of(Entity entity, double distance) {
        EntityType<?> kind = entity.getType();
        return new Target(entity, kind, label(entity, kind), distance);
    }

    public boolean sameKindAs(Target other) {
        return other != null && kind == other.kind;
    }

    private static String label(Entity entity, EntityType<?> kind) {
        String name = entity.getName().getString();
        return name == null || name.isBlank() ? kind.getDescription().getString() : name;
    }
}