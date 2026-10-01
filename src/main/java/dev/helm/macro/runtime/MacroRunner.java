package dev.helm.macro.runtime;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import dev.helm.input.InputBinding;
import dev.helm.macro.MacroDocument;
import dev.helm.macro.MacroStatement;

final class MacroRunner {

    private final String name;
    private final Deque<MacroFrame> frames = new ArrayDeque<>();

    private final List<InputBinding> held = new ArrayList<>();

    private long remainingWait;
    private boolean finished;
    private Ongoing ongoing;

    interface Ongoing {

        boolean done();
    }

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

    void letGoOfHeldInputs() {
        for (InputBinding input : held) {
            try {
                MacroActions.press(input, false);
            } catch (RuntimeException gone) {
                continue;
            }
        }
        held.clear();
    }

    void tick() {
        if (finished) {
            return;
        }

        if (remainingWait > 0L) {
            remainingWait--;
            return;
        }

        if (ongoing != null) {
            if (ongoing.done()) {
                ongoing = null;
            }
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
            case MacroStatement.Move move -> ongoing = MacroActions.walk(move);
            case MacroStatement.Anchor anchor -> ongoing = MacroActions.anchor(anchor);
            case MacroStatement.Look look -> MacroActions.look(look);
            case MacroStatement.Gaze gaze -> MacroActions.gaze(gaze);
            case MacroStatement.Hold hold -> {
                MacroActions.press(hold.input(), true);
                if (!held.contains(hold.input())) {
                    held.add(hold.input());
                }
            }
            case MacroStatement.Release release -> {
                MacroActions.press(release.input(), false);
                held.remove(release.input());
            }
            case MacroStatement.Press press -> ongoing = MacroActions.tap(press.input());
        }
    }
}
