package dev.helm.setting;

import dev.helm.command.ArgumentAccess;
import dev.helm.command.ArgumentDefinition;
import dev.helm.command.ArgumentType;
import dev.helm.command.Command;
import dev.helm.command.CommandCall;
import dev.helm.command.CommandException;
import dev.helm.command.CommandFeedback;
import dev.helm.command.CommandResult;
import dev.helm.storage.HelmStorage;
import dev.helm.tools.BlockAvoidList;

public final class SettingsCommand {

    private SettingsCommand() {
    }

    public static Command build() {
        return Command.group("settings").also("setting", "option").describedAs("Reads and changes settings.")
                .containing(get(), set(), list(), reset());
    }

    private static Command get() {
        return Command.leaf("get", call -> {
            String name = call.arguments().requireString("name");
            Setting<?> setting = find(name);
            if (setting == null) {
                call.output().error(CommandFeedback.unknownSetting(name));
                return CommandResult.FAILURE;
            }
            call.output().feedback(net.minecraft.network.chat.Component.literal(setting.describe()));
            return CommandResult.SUCCESS;
        }).describedAs("Shows one setting and its current value.")
                .taking(ArgumentDefinition.required("name", ArgumentType.STRING, "setting name"));
    }

    private static Command set() {
        return changeCommand();
    }

    private static CommandResult change(CommandCall call) {
        ArgumentAccess arguments = call.arguments();
        String name = arguments.requireString("name");
        Setting<?> setting = find(name);
        if (setting == null) {
            call.output().error(CommandFeedback.unknownSetting(name));
            return CommandResult.FAILURE;
        }
        String value = arguments.requireString("value");
        setting.accept(parse(setting, value.trim()));
        call.output().feedback(net.minecraft.network.chat.Component.literal(setting.describe()));
        BlockAvoidList.refresh(Settings.holder().mining());
        SettingsFile.save(HelmStorage.root().resolve(SettingsFile.fileName()));
        return CommandResult.SUCCESS;
    }

    public static Command changeCommand() {
        return Command.leaf("set", SettingsCommand::change)
                .describedAs("Changes one setting.")
                .taking(
                        ArgumentDefinition.required("name", ArgumentType.STRING, "setting name"),
                        ArgumentDefinition.required("value", ArgumentType.STRING, "new value"));
    }

    private static Command list() {
        return Command.leaf("list", call -> {
            call.output().feedback(net.minecraft.network.chat.Component.literal(SettingsFile.describe()));
            return CommandResult.SUCCESS;
        }).describedAs("Lists every setting and its current value.");
    }

    private static Command reset() {
        return Command.leaf("reset", call -> {
            Settings.holder().restoreDefaults();
            BlockAvoidList.refresh(Settings.holder().mining());
            call.output().feedback(net.minecraft.network.chat.Component.literal(
                    "Settings restored to defaults."));
            return CommandResult.SUCCESS;
        }).also("defaults").describedAs("Restores every setting to its default value.");
    }

    private static Setting<?> find(String name) {
        for (SettingSection section : Settings.holder().sections()) {
            Setting<?> setting = section.find(name);
            if (setting != null) {
                return setting;
            }
        }
        return null;
    }

    private static Object parse(Setting<?> setting, String value) {
        try {
            return switch (setting.kind()) {
                case BOOLEAN -> Boolean.valueOf(value);
                case WHOLE -> Integer.valueOf(value);
                case DECIMAL -> Double.valueOf(value);
                case TEXT -> value;
            };
        } catch (NumberFormatException invalid) {
            throw new CommandException(CommandFeedback.invalidArgument(setting.key(), value));
        }
    }
}
