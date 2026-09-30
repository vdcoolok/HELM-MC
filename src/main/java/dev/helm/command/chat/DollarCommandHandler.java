package dev.helm.command.chat;

import java.util.List;
import java.util.Optional;

import dev.helm.command.CommandCall;
import dev.helm.command.CommandDefinition;
import dev.helm.command.CommandException;
import dev.helm.command.CommandFeedback;
import dev.helm.command.CommandOutput;
import dev.helm.command.CommandRegistry;
import dev.helm.command.CommandResult;
import dev.helm.command.CommandRoot;
import dev.helm.command.help.CommandHelp;

public final class DollarCommandHandler {

    private final CommandRegistry registry;
    private final CommandOutput output;

    public DollarCommandHandler(CommandRegistry registry, CommandOutput output) {
        this.registry = registry;
        this.output = output;
    }

    public CommandResult handle(String body) {
        String trimmed = body.trim();
        if (trimmed.isEmpty()) {
            CommandHelp.send(registry, CommandRoot.NAME, output);
            return CommandResult.SUCCESS;
        }

        List<String> tokens = LineTokenizer.tokenize(trimmed);
        String label = tokens.get(0);
        Optional<CommandDefinition> found = registry.find(label);
        if (found.isEmpty()) {
            output.error(CommandFeedback.unknownCommand(label));
            return CommandResult.FAILURE;
        }

        CommandDefinition definition = found.get();
        try {
            CommandCall call = new CommandCall(definition, registry, label,
                    TokenArguments.resolve(definition.arguments(), tokens.subList(1, tokens.size())), output);
            return definition.executor().execute(call);
        } catch (CommandException failure) {
            output.error(failure.message());
            return CommandResult.FAILURE;
        } catch (RuntimeException unexpected) {
            output.error(CommandFeedback.error("Command failed: " + unexpected));
            return CommandResult.FAILURE;
        }
    }
}
