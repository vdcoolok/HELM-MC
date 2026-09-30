package dev.helm.command;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public final class CommandTheme {

    public static final int RED_BROWN = 0xA52A2A;

    private CommandTheme() {
    }

    public static MutableComponent brand() {
        return Component.literal(CommandFeedback.PREFIX).withColor(RED_BROWN);
    }

    public static MutableComponent heading() {
        return Component.literal("HELM").withColor(RED_BROWN);
    }

    public static MutableComponent commandName() {
        return Component.literal("").withStyle(ChatFormatting.WHITE);
    }

    public static MutableComponent description() {
        return Component.literal("").withStyle(ChatFormatting.DARK_GRAY);
    }
}
