package dev.helm.command.popup;

import java.util.ArrayList;
import java.util.List;

import dev.helm.setting.SettingCatalogue;
import dev.helm.setting.SettingKind;

public final class SettingPicker {

    private SettingPicker() {
    }

    public static List<PopupRow> rows(String partial) {
        List<PopupRow> built = new ArrayList<>();
        for (SettingCatalogue.Entry entry : SettingCatalogue.matching(partial)) {
            built.add(new PopupRow(entry.key(), entry.shown(), 0, entry.key()));
        }
        return built;
    }

    public static List<PopupRow> values(SettingCatalogue.Entry entry, String partial) {
        List<PopupRow> built = new ArrayList<>();
        if (entry == null) {
            return built;
        }
        for (String candidate : candidates(entry)) {
            built.add(new PopupRow(candidate, "", 0, candidate));
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

    public static List<String> details(PopupRow row) {
        SettingCatalogue.Entry entry = SettingCatalogue.find(row.label());
        if (entry == null) {
            return List.of();
        }
        List<String> lines = new ArrayList<>();
        lines.add(entry.label());
        lines.add("");
        lines.add(entry.detail());
        lines.add("");
        lines.add("currently " + entry.shown());
        lines.add(entry.isDefault() ? "default " + initial(entry) : "default " + initial(entry)
                + ", changed");
        return lines;
    }

    private static String initial(SettingCatalogue.Entry entry) {
        Object value = entry.setting().initial();
        return value == null ? "" : String.valueOf(value);
    }
}