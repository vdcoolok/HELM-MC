package dev.helm.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dev.helm.command.builtin.ExitEditModeCommand;
import dev.helm.command.builtin.HelpCommand;
import dev.helm.command.builtin.MacroCommand;
import dev.helm.command.builtin.VersionCommand;
import dev.helm.command.chat.DollarPrefix;
import dev.helm.navigate.AutoCommands;
import dev.helm.navigate.GoToCommand;
import dev.helm.navigate.StopCommand;
import dev.helm.setting.SettingsCommand;
import dev.helm.command.chat.SystemMessageOutput;
import dev.helm.storage.HelmStorage;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public final class CommandSystem {

    private CommandSystem() {
    }

    public static void start() {
        List<Command> commands = new ArrayList<>();
        commands.add(HelpCommand.build());
        commands.add(MacroCommand.build());
        commands.add(VersionCommand.build());
        commands.add(ExitEditModeCommand.build());
        commands.add(GoToCommand.build());
        commands.addAll(Arrays.asList(AutoCommands.all()));
        commands.add(StopCommand.build());
        commands.add(SettingsCommand.build());
        commands.add(SettingsCommand.changeCommand());
        CommandTree.instance().install(commands.toArray(new Command[0]));
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> HelmStorage.prepare()
                .ifPresent(problem -> new SystemMessageOutput(client).error(Component.literal(problem))));
    }

    public static boolean isCommandLine(String message) {
        return DollarPrefix.isCommand(message);
    }

    public static CommandResult dispatch(Minecraft client, String message) {
        return CommandTree.instance().dispatch(DollarPrefix.body(message), new SystemMessageOutput(client));
    }
}
