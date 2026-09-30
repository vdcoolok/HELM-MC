package dev.helm.macro.runtime;

import java.util.Optional;

import dev.helm.command.CommandFeedback;
import dev.helm.command.CommandOutput;
import dev.helm.macro.MacroDocument;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public final class MacroController {

    private static final MacroController INSTANCE = new MacroController();

    private MacroRunner runner;
    private CommandOutput output;
    private boolean installed;

    private MacroController() {
    }

    public static MacroController instance() {
        return INSTANCE;
    }

    public void install() {
        if (installed) {
            return;
        }
        installed = true;
        ClientTickEvents.END_CLIENT_TICK.register(client -> INSTANCE.tick());
    }

    public void start(MacroDocument document, CommandOutput output) {
        runner = new MacroRunner(document);
        this.output = output;
        output.feedback(CommandFeedback.success("Started macro: " + document.name()));
    }

    public void stop(CommandOutput output) {
        if (runner == null) {
            output.error(CommandFeedback.error("No macro is running"));
            return;
        }
        String name = runner.name();
        clear();
        output.feedback(CommandFeedback.success("Stopped macro: " + name));
    }

    public Optional<String> active() {
        return runner == null ? Optional.empty() : Optional.of(runner.name());
    }

    public void tick() {
        if (runner == null) {
            return;
        }

        try {
            runner.tick();
        } catch (MacroFailure failure) {
            fail(failure.getMessage());
            return;
        }

        if (runner.finished()) {
            succeed("Finished macro: " + runner.name());
        }
    }

    private void fail(String message) {
        CommandOutput target = output;
        clear();
        if (target != null) {
            target.error(CommandFeedback.error(message));
        }
    }

    private void succeed(String message) {
        CommandOutput target = output;
        clear();
        if (target != null) {
            target.feedback(CommandFeedback.success(message));
        }
    }

    private void clear() {
        runner = null;
        output = null;
    }
}
