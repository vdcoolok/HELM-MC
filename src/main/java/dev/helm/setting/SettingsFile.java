package dev.helm.setting;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class SettingsFile {

    private static final String SUFFIX = ".conf";
    private static final String TEMP_SUFFIX = ".tmp";

    private SettingsFile() {
    }

    public static void load(Path file) {
        if (!Files.isRegularFile(file)) {
            return;
        }
        try {
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                apply(line);
            }
        } catch (IOException unreadable) {
            Settings.holder().restoreDefaults();
        }
    }

    public static void save(Path file) {
        List<String> lines = new ArrayList<>();
        for (SettingSection section : Settings.holder().sections()) {
            for (Setting<?> setting : section.declared()) {
                lines.add(setting.key() + " = " + setting.value());
            }
        }
        try {
            Path parent = file.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Path temporary = file.resolveSibling(file.getFileName() + TEMP_SUFFIX);
            Files.write(temporary, lines, StandardCharsets.UTF_8);
            Files.move(temporary, file, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException unwritable) {
            SettingsSaveFailure.report(file, unwritable);
        }
    }

    private static void apply(String line) {
        String trimmed = line.trim();
        if (trimmed.isEmpty() || trimmed.startsWith("#")) {
            return;
        }
        int split = trimmed.indexOf('=');
        if (split < 0) {
            return;
        }
        String key = trimmed.substring(0, split).trim();
        String value = trimmed.substring(split + 1).trim();
        for (SettingSection section : Settings.holder().sections()) {
            if (section.find(key) != null) {
                section.apply(key, value);
                return;
            }
        }
    }

    public static String describe() {
        List<String> lines = new ArrayList<>();
        for (SettingSection section : Settings.holder().sections()) {
            for (Setting<?> setting : section.declared()) {
                lines.add(setting.describe());
            }
        }
        return String.join(System.lineSeparator(), lines);
    }

    public static String fileName() {
        return "settings" + SUFFIX;
    }

    static String normalise(String key) {
        return key.toLowerCase(Locale.ROOT);
    }
}
