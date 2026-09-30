package dev.helm.macro;

import java.util.List;

public record MacroDocument(String name, List<MacroStatement> statements) {

    public MacroDocument {
        statements = List.copyOf(statements);
    }
}
