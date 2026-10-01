package dev.helm.macro.edit;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import dev.helm.macro.MacroLinesEdit;
import dev.helm.macro.MacroStoreException;
import dev.helm.macro.MacroParser;
import dev.helm.macro.MacroSyntax;
import dev.helm.macro.MacroSyntaxException;
import dev.helm.macro.MacroTarget;
import dev.helm.storage.MacroStore;

public final class MacroEditor {

    private static final MacroEditor INSTANCE = new MacroEditor();

    private String editing;

    private MacroEditor() {
    }

    public static MacroEditor instance() {
        return INSTANCE;
    }

    public void begin(String name) {
        editing = name;
    }

    public void end() {
        editing = null;
    }

    public boolean isEditing() {
        return editing != null;
    }

    public Optional<String> target() {
        return Optional.ofNullable(editing);
    }

    public void create(String name) {
        if (MacroStore.exists(name)) {
            throw new MacroStoreException("A macro named " + name + " already exists", null);
        }
        MacroStore.write(name, List.of());
    }

    public void append(String line) {
        String keyword = keywordOf(line);
        if (!MacroSyntax.isKeyword(keyword)) {
            throw new MacroSyntaxException("Unknown command '" + keyword + "'. Available: "
                    + MacroSyntax.describe());
        }
        String stored = MacroTarget.resolve(line);
        if (!MacroSyntax.isKeyword(keywordOf(stored))) {
            throw new MacroSyntaxException("Unknown command '" + keywordOf(stored) + "'");
        }
        MacroStore.write(editing, MacroLinesEdit.added(MacroStore.read(editing), stored));
    }

    public void remove(int position) {
        MacroStore.write(editing, MacroLinesEdit.removed(MacroStore.read(editing), position));
    }

    public void move(int from, int to) {
        MacroStore.write(editing, MacroLinesEdit.moved(MacroStore.read(editing), from, to));
    }

    public List<String> lines() {
        requireTarget();
        return MacroStore.read(editing);
    }

    public Optional<String> problem() {
        try {
            requireTarget();
            MacroParser.parse(editing, MacroStore.read(editing));
            return Optional.empty();
        } catch (MacroSyntaxException invalid) {
            return Optional.of(invalid.getMessage());
        } catch (IllegalStateException noTarget) {
            return Optional.of(noTarget.getMessage());
        }
    }

    private void requireTarget() {
        if (editing == null) {
            throw new IllegalStateException("No macro is open for editing");
        }
    }

    private static String keywordOf(String line) {
        String trimmed = line.trim();
        int space = trimmed.indexOf(' ');
        return (space < 0 ? trimmed : trimmed.substring(0, space)).toLowerCase(Locale.ROOT);
    }
}
