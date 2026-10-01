package dev.helm.command;

import dev.helm.command.builtin.ExitEditModeCommand;
import dev.helm.command.builtin.HelpCommand;
import dev.helm.command.builtin.MacroCommand;
import dev.helm.command.builtin.VersionCommand;
import dev.helm.command.chat.DollarPrefix;
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
        CommandTree.instance().install(
                HelpCommand.build(),
                MacroCommand.build(),
                VersionCommand.build(),
                ExitEditModeCommand.build(),
                GoToCommand.build(),
                StopCommand.build(),
                SettingsCommand.build(),
                SettingsCommand.changeCommand());
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
