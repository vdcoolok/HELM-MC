package dev.helm.command.builtin;

import dev.helm.command.Command;
import dev.helm.command.CommandResult;
import dev.helm.command.CommandTree;
import dev.helm.command.help.CommandHelp;

public final class HelpCommand {

    private HelpCommand() {
    }

    public static Command build() {
        return Command.leaf("help", call -> {
            CommandHelp.send(CommandTree.instance(), call.output());
            return CommandResult.SUCCESS;
        }).also("h", "?").describedAs("Lists every available command.");
    }
}
