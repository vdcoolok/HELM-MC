package dev.helm.follow.subject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class MobTypes {

    public record Choice(String name, String note, String search) {
    }

    private static List<Choice> cached;

    private MobTypes() {
    }

    public static List<Choice> all() {
        if (cached != null) {
            return cached;
        }
        List<Choice> found = new ArrayList<>();
        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            if (type.getCategory() == MobCategory.MISC) {
                continue;
            }
            Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
            if (id == null) {
                continue;
            }
            String name = id.getPath();
            String note = type.getCategory().getSerializedName();
            found.add(new Choice(name, note, (name + " " + note).toLowerCase(Locale.ROOT)));
        }
        found.sort((one, two) -> one.name().compareTo(two.name()));
        cached = List.copyOf(found);
        return cached;
    }

    public static List<Choice> matching(String partial) {
        String wanted = partial == null ? "" : partial.trim().toLowerCase(Locale.ROOT);
        if (wanted.isEmpty()) {
            return all();
        }
        List<Choice> found = new ArrayList<>();
        for (Choice choice : all()) {
            if (choice.search().contains(wanted)) {
                found.add(choice);
            }
        }
        return found;
    }

    public static Identifier identifying(String written) {
        Identifier id = EntityTypeNames.identifierOf(written);
        if (id == null || !BuiltInRegistries.ENTITY_TYPE.containsKey(id)) {
            return null;
        }
        return isMob(BuiltInRegistries.ENTITY_TYPE.getValue(id)) ? id : null;
    }

    private static boolean isMob(EntityType<?> type) {
        return type.getCategory() != MobCategory.MISC;
    }
}