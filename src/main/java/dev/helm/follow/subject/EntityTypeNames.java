package dev.helm.follow.subject;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;

public final class EntityTypeNames {

    private static final String DEFAULT_NAMESPACE = "minecraft:";

    private EntityTypeNames() {
    }

    public static Identifier identifierOf(String written) {
        if (written == null || written.isBlank()) {
            return null;
        }
        String trimmed = written.trim();
        String full = trimmed.contains(":") ? trimmed : DEFAULT_NAMESPACE + trimmed;
        Identifier id = Identifier.tryParse(full);
        if (id == null || !BuiltInRegistries.ENTITY_TYPE.containsKey(id)) {
            return null;
        }
        return id;
    }

    public static Identifier identifierOf(Entity entity) {
        return BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
    }
}