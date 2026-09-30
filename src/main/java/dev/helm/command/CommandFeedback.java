package dev.helm.command;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public final class CommandFeedback {

    public static final String PREFIX = "[HELM] ";

    private CommandFeedback() {
    }

    public static MutableComponent error(String message) {
        return prefixed(message, ChatFormatting.RED);
    }

    public static MutableComponent success(String message) {
        return prefixed(message, ChatFormatting.GREEN);
    }

    public static MutableComponent info(String message) {
        return prefixed(message, ChatFormatting.GRAY);
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

    private static MutableComponent prefixed(String message, ChatFormatting colour) {
        MutableComponent line = Component.literal(PREFIX);
        line.withStyle(ChatFormatting.DARK_AQUA);
        return line.append(Component.literal(message).withStyle(colour));
    }
}
