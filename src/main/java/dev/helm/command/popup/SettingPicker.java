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
        List<PopupRow> packed = new ArrayList<>();
        int column = 0;
        int inColumn = 0;
        for (SettingCatalogue.Entry entry : SettingCatalogue.matching(partial)) {
            if (inColumn >= PopupTheme.MAX_ROWS) {
                column++;
                inColumn = 0;
            }
            packed.add(new PopupRow(entry.key(), entry.shown(), column, entry.key()));
            inColumn++;
        }
        return packed;
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

    private static boolean startsWith(String candidate, String partial) {
        if (partial == null || partial.isEmpty()) {
            return true;
        }
        return candidate.toLowerCase(Locale.ROOT)
                .startsWith(partial.toLowerCase(Locale.ROOT));
    }
}