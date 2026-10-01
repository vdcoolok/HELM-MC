package dev.helm.command.popup;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dev.helm.setting.SettingCatalogue;
import dev.helm.setting.SettingKind;

public final class SettingPicker {

    private SettingPicker() {
    }

    public static List<PopupRow> rows(String partial) {
        List<PopupRow> built = new ArrayList<>();
        List<String> sections = sectionNames();
        for (SettingCatalogue.Entry entry : SettingCatalogue.matching(partial)) {
            built.add(new PopupRow(entry.key(), entry.shown(), columnOf(entry, sections),
                    entry.key()));
        }
        return built;
    }

    public static List<PopupRow> values(SettingCatalogue.Entry entry, String partial) {
        List<PopupRow> built = new ArrayList<>();
        if (entry == null) {
            return built;
        }
        for (String candidate : candidates(entry)) {
            if (startsWith(candidate, partial)) {
                built.add(new PopupRow(candidate, "", 0, candidate));
            }
        }
        return built;
    }

    public static List<String> candidates(SettingCatalogue.Entry entry) {
        if (entry.kind() != SettingKind.BOOLEAN) {
            return List.of(entry.shown());
        }
        boolean on = Boolean.TRUE.equals(entry.value());
        return on ? List.of("false", "true") : List.of("true", "false");
    }

    public static List<String> sectionNames() {
        List<String> names = new ArrayList<>();
        for (SettingCatalogue.Entry entry : SettingCatalogue.all()) {
            if (!names.contains(entry.section())) {
                names.add(entry.section());
            }
        }
        return names;
    }

    private static int columnOf(SettingCatalogue.Entry entry, List<String> sections) {
        int index = sections.indexOf(entry.section());
        return index < 0 ? 0 : index;
    }

    private static boolean startsWith(String candidate, String partial) {
        if (partial == null || partial.isEmpty()) {
            return true;
        }
        return candidate.toLowerCase(Locale.ROOT)
                .startsWith(partial.toLowerCase(Locale.ROOT));
    }
}