package dev.helm.macro;

import java.util.ArrayList;
import java.util.List;

final class MacroLines {

    private final List<MacroLine> lines;
    private int index;

    MacroLines(List<MacroLine> lines) {
        this.lines = lines;
    }

    boolean hasNext() {
        return index < lines.size();
    }

    MacroLine peek() {
        return lines.get(index);
    }

    MacroLine next() {
        return lines.get(index++);
    }

    void expectEndLoop() {
        if (!hasNext()) {
            throw new MacroSyntaxException("'loop' is missing 'endloop'");
        }
        MacroLine line = next();
        if (!line.keyword().equals("endloop")) {
            throw new MacroSyntaxException("'loop' is missing 'endloop' before line " + line.number());
        }
    }

    List<MacroLine> remaining() {
        return new ArrayList<>(lines.subList(index, lines.size()));
    }
}
