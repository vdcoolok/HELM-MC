package dev.helm.macro.runtime;

import java.util.ArrayDeque;
import java.util.Deque;

import dev.helm.macro.MacroDocument;
import dev.helm.macro.MacroStatement;

final class MacroRunner {

    private final String name;
    private final Deque<MacroFrame> frames = new ArrayDeque<>();

    private long remainingWait;
    private boolean finished;

    MacroRunner(MacroDocument document) {
        this.name = document.name();
        this.frames.push(MacroFrame.root(document.statements()));
    }

    String name() {
        return name;
    }

    boolean finished() {
        return finished;
    }

    void tick() {
        if (finished) {
            return;
        }

        if (remainingWait > 0L) {
            remainingWait--;
            return;
        }

        while (!frames.isEmpty()) {
            MacroFrame frame = frames.peek();
            if (frame.hasNext()) {
                execute(frame.next());
                return;
            }
            if (!frame.isLoop()) {
                frames.pop();
                continue;
            }
            if (!frame.shouldRepeat()) {
                frames.pop();
                continue;
            }
            frame.rewind();
        }

        finished = true;
    }

    private void execute(MacroStatement statement) {
        switch (statement) {
            case MacroStatement.Wait wait -> remainingWait = wait.duration().ticks();
            case MacroStatement.Loop loop -> frames.push(MacroFrame.loop(loop.body(), loop.repeats()));
            default -> throw MacroFailure.unavailable(statement);
        }
    }
}
