package dev.helm.macro;

import java.util.ArrayList;
import java.util.List;

public final class MacroLinesEdit {

    private MacroLinesEdit() {
    }

    public static List<String> added(List<String> lines, String line) {
        List<String> updated = new ArrayList<>(lines);
        updated.add(line);
        return updated;
    }

    public static List<String> removed(List<String> lines, int position) {
        List<String> updated = new ArrayList<>(lines);
        updated.remove(requireIndex(position, lines.size()));
        return updated;
    }

    public static List<String> moved(List<String> lines, int from, int to) {
        List<String> updated = new ArrayList<>(lines);
        String moved = updated.remove(requireIndex(from, lines.size()));
        updated.add(requireIndex(to, updated.size() + 1), moved);
        return updated;
    }

    private static int requireIndex(int position, int size) {
        if (position < 1 || position > size) {
            throw new IllegalArgumentException("There is no line " + position + " (the macro has " + size + ")");
        }
        return position - 1;
    }
}
