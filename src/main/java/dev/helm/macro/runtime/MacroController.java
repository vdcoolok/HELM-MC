package dev.helm.macro.runtime;

import java.util.Optional;

import dev.helm.command.CommandFeedback;
import dev.helm.command.CommandOutput;
import dev.helm.macro.MacroDocument;
import dev.helm.navigate.NavigatorAgent;
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

    public void halt() {
        clear();
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
        stopWalking();
        if (target != null) {
            target.error(CommandFeedback.error(message));
        }
    }

    private void succeed(String message) {
        CommandOutput target = output;
        clear();
        stopWalking();
        if (target != null) {
            target.feedback(CommandFeedback.success(message));
        }
    }

    private void stopWalking() {
        NavigatorAgent agent = NavigatorAgent.instance();
        agent.pilot().halt();
        agent.pilot().forgetDestination();
        agent.navigator().cancelSearch();
    }

    private void clear() {
        if (runner != null) {
            runner.letGoOfHeldInputs();
        }
        runner = null;
        output = null;
    }
}
