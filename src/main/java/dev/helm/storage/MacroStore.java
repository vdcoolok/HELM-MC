package dev.helm.storage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import dev.helm.macro.MacroName;
import dev.helm.macro.MacroStoreException;

public final class MacroStore {

    private MacroStore() {
    }

    public static Path directory() {
        return HelmStorage.macroDirectory();
    }

    public static List<String> list() {
        return list(directory());
    }

    public static List<String> list(Path directory) {
        if (!Files.isDirectory(directory)) {
            return List.of();
        }
        try (Stream<Path> files = Files.list(directory)) {
            List<String> names = new ArrayList<>();
            files.filter(MacroStore::isMacroFile)
                    .filter(Files::isRegularFile)
                    .map(path -> fileNameOf(path.getFileName().toString()))
                    .sorted()
                    .forEach(names::add);
            return names;
        } catch (IOException unreadable) {
            throw new MacroStoreException("Could not read the macro folder", unreadable);
        }
    }

    public static boolean exists(String requested) {
        String name = MacroName.normalise(requested);
        return MacroName.isValid(name) && Files.isRegularFile(pathFor(name));
    }

    public static List<String> read(String requested) {
        String name = MacroName.normalise(requested);
        MacroName.validate(name);
        if (!Files.isRegularFile(pathFor(name))) {
            throw new MacroStoreException("No macro named " + name, null);
        }
        try {
            return Files.readAllLines(pathFor(name), StandardCharsets.UTF_8);
        } catch (IOException unreadable) {
            throw new MacroStoreException("Could not read " + name, unreadable);
        }
    }

    public static void write(String requested, List<String> lines) {
        String name = MacroName.normalise(requested);
        MacroName.validate(name);
        Path file = pathFor(name);
        try {
            Files.createDirectories(file.getParent());
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException unwritable) {
            throw new MacroStoreException("Could not create the macro " + name, unwritable);
        }
    }

    private static Path pathFor(String name) {
        return directory().resolve(name + MacroName.SUFFIX);
    }

    private static boolean isMacroFile(Path path) {
        return path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(MacroName.SUFFIX);
    }

    private static String fileNameOf(String fileName) {
        return fileName.substring(0, fileName.length() - MacroName.SUFFIX.length());
    }
}
