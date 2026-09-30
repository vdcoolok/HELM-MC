package dev.helm.command;

import dev.helm.command.builtin.HelpCommand;
import dev.helm.command.builtin.VersionCommand;
import dev.helm.command.brigadier.ClientCommandRegistrar;
import dev.helm.command.chat.DollarCommandHandler;
import dev.helm.command.chat.DollarPrefix;
import dev.helm.command.chat.SystemMessageOutput;
import net.minecraft.client.Minecraft;

public final class CommandSystem {

    private static final CommandRegistry REGISTRY = new CommandRegistry();

    private CommandSystem() {
    }

    public static void start() {
        HelpCommand.register(REGISTRY);
        VersionCommand.register(REGISTRY);
        ClientCommandRegistrar.register(CommandRoot.NAME, REGISTRY);
    }

    public static boolean isCommandLine(String message) {
        return DollarPrefix.isCommand(message);
    }

    public static CommandResult dispatch(Minecraft client, String message) {
        CommandOutput output = new SystemMessageOutput(client);
        return new DollarCommandHandler(REGISTRY, output).handle(DollarPrefix.body(message));
    }
}
