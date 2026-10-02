package dev.helm.setting;

import java.util.ArrayList;
import java.util.List;

public final class SettingCatalogue {

    public record Entry(String section, Setting<?> setting) {

        public String key() {
            return setting.key();
        }

        public String label() {
            return setting.label();
        }

        public String detail() {
            return setting.detail();
        }

        public Object value() {
            return setting.value();
        }

        public SettingKind kind() {
            return setting.kind();
        }

        public boolean isDefault() {
            Object initial = setting.initial();
            return initial == null ? value() == null : initial.equals(value());
        }

        public String shown() {
            Object current = value();
            return current == null ? "" : String.valueOf(current);
        }

        public String summary() {
            return label() + " = " + shown() + "  (" + detail() + ")";
        }
    }

    private SettingCatalogue() {
    }

    public static List<Entry> all() {
        List<Entry> entries = new ArrayList<>();
        for (SettingSection section : Settings.holder().sections()) {
            String name = sectionName(section);
            for (Setting<?> setting : section.declared()) {
                entries.add(new Entry(name, setting));
            }
        }
        return entries;
    }

    public static Entry find(String name) {
        if (name == null) {
            return null;
        }
        String wanted = SettingsFile.normalise(name.trim());
        if (wanted.isEmpty()) {
            return null;
        }
        for (Entry entry : all()) {
            for (String candidate : namesFor(entry)) {
                if (SettingsFile.normalise(candidate).equals(wanted)) {
                    return entry;
                }
            }
        }
        return null;
    }

    public static String sectionName(SettingSection section) {
        List<Setting<?>> declared = section.declared();
        if (declared.isEmpty()) {
            return "";
        }
        String key = declared.get(0).key();
        int dot = key.indexOf('.');
        return dot < 0 ? key : key.substring(0, dot);
    }

    public static List<String> namesFor(Entry entry) {
        List<String> names = new ArrayList<>();
        names.add(entry.key());
        String bare = entry.section();
        if (entry.key().startsWith(bare + ".")) {
            String shortName = entry.key().substring(bare.length() + 1);
            if (shortName.length() >= 3) {
                names.add(shortName);
            }
        }
        return names;
    }

    public static List<Entry> matching(String partial) {
        return SettingSearch.rank(all(), partial);
    }
}