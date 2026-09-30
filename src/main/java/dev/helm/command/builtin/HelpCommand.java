package dev.helm.command.builtin;

import java.util.List;

import dev.helm.command.CommandDefinition;
import dev.helm.command.CommandRegistry;
import dev.helm.command.CommandResult;
import dev.helm.command.CommandRoot;
import dev.helm.command.help.CommandHelp;

public final class HelpCommand {

    private HelpCommand() {
    }

    public static void register(CommandRegistry registry) {
        registry.register(new CommandDefinition("help", List.of("h", "?"),
                "Lists every available command.",
                List.of(),
                call -> {
                    call.output().feedback(CommandHelp.render(call.registry(), CommandRoot.NAME));
                    return CommandResult.SUCCESS;
                }));
    }
}
