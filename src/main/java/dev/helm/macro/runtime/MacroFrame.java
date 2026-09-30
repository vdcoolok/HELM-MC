package dev.helm.macro.runtime;

import java.util.List;

import dev.helm.macro.MacroStatement;

final class MacroFrame {

    static final long UNBOUNDED = -1L;

    private final List<MacroStatement> statements;
    private final boolean loop;
    private final long repeats;

    private int index;
    private long passes;

    MacroFrame(List<MacroStatement> statements, boolean loop, long repeats) {
        this.statements = statements;
        this.loop = loop;
        this.repeats = repeats;
    }

    static MacroFrame root(List<MacroStatement> statements) {
        return new MacroFrame(statements, false, UNBOUNDED);
    }

    static MacroFrame loop(List<MacroStatement> body, long repeats) {
        return new MacroFrame(body, true, repeats);
    }

    boolean hasNext() {
        return index < statements.size();
    }

    MacroStatement next() {
        return statements.get(index++);
    }

    boolean exhausted() {
        return index >= statements.size();
    }

    boolean isLoop() {
        return loop;
    }

    boolean shouldRepeat() {
        return repeats == UNBOUNDED || passes + 1 < repeats;
    }

    void rewind() {
        index = 0;
        passes++;
    }
}
