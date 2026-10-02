package dev.helm.command.popup;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;

import dev.helm.setting.SettingCatalogue;

public final class BlockPicker {

    private static List<Choice> cached;

    private BlockPicker() {
    }

    public record Choice(String path, String shown, String search) {
    }

    public static List<PopupRow> rows(SettingCatalogue.Entry entry, String partial) {
        List<String> chosen = chosen(entry);
        List<PopupRow> built = new ArrayList<>();
        for (Choice choice : all()) {
            if (!matches(choice, partial)) {
                continue;
            }
            String note = chosen.contains(choice.path()) ? "chosen" : "";
            built.add(new PopupRow(choice.shown(), note, 0, choice.path()));
        }
        return built;
    }

    public static String toggle(SettingCatalogue.Entry entry, PopupRow row) {
        if (entry == null || row == null || row.insert().isEmpty()) {
            return null;
        }
        List<String> chosen = new ArrayList<>(chosen(entry));
        if (!chosen.remove(row.insert())) {
            chosen.add(row.insert());
        }
        String joined = String.join(", ", chosen);
        entry.setting().accept(joined);
        return joined;
    }

    public static List<String> chosen(SettingCatalogue.Entry entry) {
        List<String> found = new ArrayList<>();
        if (entry == null) {
            return found;
        }
        String current = entry.shown();
        if (current == null || current.isBlank()) {
            return found;
        }
        for (String piece : current.split(",")) {
            String name = piece.trim();
            if (name.isEmpty()) {
                continue;
            }
            if (!found.contains(name)) {
                found.add(name);
            }
        }
        return found;
    }

    private static boolean matches(Choice choice, String partial) {
        String wanted = partial == null ? "" : partial.trim().toLowerCase(Locale.ROOT);
        if (wanted.isEmpty()) {
            return true;
        }
        return choice.search().contains(wanted);
    }

    private static List<Choice> all() {
        if (cached != null) {
            return cached;
        }
        List<Choice> found = new ArrayList<>();
        for (Block block : BuiltInRegistries.BLOCK) {
            if (!(block.asItem() instanceof BlockItem)) {
                continue;
            }
            Identifier id = BuiltInRegistries.BLOCK.getKey(block);
            if (id == null) {
                continue;
            }
            String path = id.toString();
            String bare = id.getPath().replace('_', ' ');
            String search = (bare + " " + path.replace('/', ' ')).toLowerCase(Locale.ROOT);
            found.add(new Choice(path, bare, search));
        }
        found.sort((one, two) -> one.shown().compareTo(two.shown()));
        cached = List.copyOf(found);
        return cached;
    }
}