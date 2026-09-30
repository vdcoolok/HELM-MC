package dev.helm.command;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public final class CommandFeedback {

    public static final String PREFIX = "[HELM] ";

    private CommandFeedback() {
    }

    public static MutableComponent error(String message) {
        return line(message, ChatFormatting.RED);
    }

    public static MutableComponent success(String message) {
        return line(message, ChatFormatting.GREEN);
    }

    public static MutableComponent info(String message) {
        return line(message, ChatFormatting.GRAY);
    }

    public static MutableComponent unknownCommand(String label) {
        return error("Unknown command: " + label);
    }

    public static MutableComponent missingArgument(String name) {
        return error("Missing required argument: " + name);
    }

    public static MutableComponent invalidArgument(String name, String token) {
        return error("Invalid value for " + name + ": " + token);
    }

    public static MutableComponent unknownSetting(String name) {
        return error("Unknown setting: " + name);
    }

    private static MutableComponent line(String message, ChatFormatting colour) {
        MutableComponent output = CommandTheme.brand();
        return output.append(Component.literal(message).withStyle(colour));
    }
}
