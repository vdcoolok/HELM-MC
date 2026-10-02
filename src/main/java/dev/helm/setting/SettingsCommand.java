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
import net.minecraft.network.chat.Component;

public final class SettingsCommand {

    private SettingsCommand() {
    }

    public static Command build() {
        return Command.group("settings").describedAs("Restores settings.")
                .containing(reset());
    }

    public static Command changeCommand() {
        return Command.leaf("set", SettingsCommand::change)
                .also("setting")
                .describedAs("Changes one setting, or opens the picker.")
                .taking(
                        ArgumentDefinition.optional("name", ArgumentType.STRING, "setting name"),
                        ArgumentDefinition.optional("value", ArgumentType.STRING, "new value"));
    }

    private static Command reset() {
        return Command.leaf("reset", call -> {
            Settings.holder().restoreDefaults();
            BlockAvoidList.refresh(Settings.holder().mining());
            save();
            call.output().feedback(Component.literal("Settings restored to defaults."));
            return CommandResult.SUCCESS;
        }).also("defaults").describedAs("Restores every setting to its default value.");
    }

    private static CommandResult change(CommandCall call) {
        ArgumentAccess arguments = call.arguments();
        if (!arguments.has("name")) {
            return SettingPickerScreen.open();
        }
        String name = arguments.optionalString("name");
        SettingCatalogue.Entry entry = SettingCatalogue.find(name);
        if (entry == null) {
            call.output().error(CommandFeedback.unknownSetting(name));
            return CommandResult.FAILURE;
        }
        if (!arguments.has("value")) {
            call.output().feedback(Component.literal(entry.summary()));
            return CommandResult.SUCCESS;
        }
        apply(call, entry, arguments.optionalString("value"));
        return CommandResult.SUCCESS;
    }

    public static CommandResult apply(CommandCall call, SettingCatalogue.Entry entry, String value) {
        store(entry, value);
        applySideEffects();
        save();
        call.output().feedback(Component.literal(entry.summary()));
        return CommandResult.SUCCESS;
    }

    private static void store(SettingCatalogue.Entry entry, String value) {
        if (entry == null) {
            return;
        }
        String trimmed = value == null ? "" : value.trim();
        if (entry.kind() == SettingKind.BLOCKS) {
            entry.setting().accept(BlockNames.separate(trimmed));
            return;
        }
        entry.setting().accept(parse(entry, trimmed));
    }

    public static void applySideEffects() {
        BlockAvoidList.refresh(Settings.holder().mining());
    }

    public static void save() {
        SettingsFile.save(HelmStorage.root().resolve(SettingsFile.fileName()));
    }

    private static Object parse(SettingCatalogue.Entry entry, String value) {
        try {
            return switch (entry.kind()) {
                case BOOLEAN -> parseBoolean(entry, value);
                case WHOLE -> Integer.valueOf(value);
                case DECIMAL -> Double.valueOf(value);
                case TEXT, BLOCKS -> value;
            };
        } catch (NumberFormatException invalid) {
            throw new CommandException(CommandFeedback.invalidArgument(entry.key(), value));
        }
    }

    private static Boolean parseBoolean(SettingCatalogue.Entry entry, String value) {
        return switch (value.toLowerCase(java.util.Locale.ROOT)) {
            case "true", "yes", "on", "1" -> Boolean.TRUE;
            case "false", "no", "off", "0" -> Boolean.FALSE;
            default -> throw new CommandException(
                    CommandFeedback.invalidArgument(entry.key(), value));
        };
    }
}