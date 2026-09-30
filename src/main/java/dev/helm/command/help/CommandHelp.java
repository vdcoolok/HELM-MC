package dev.helm.command.help;

import java.util.ArrayList;
import java.util.List;

import dev.helm.command.ArgumentDefinition;
import dev.helm.command.CommandDefinition;
import dev.helm.command.CommandOutput;
import dev.helm.command.CommandRegistry;
import dev.helm.command.CommandTheme;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;

public final class CommandHelp {

    private CommandHelp() {
    }

    public static void send(CommandRegistry registry, String root, CommandOutput output) {
        output.feedback(header(root));
        for (MutableComponent entry : entries(registry)) {
            output.feedback(entry);
        }
    }

    public static Component header(String root) {
        MutableComponent header = CommandTheme.brand();
        header.append(Component.literal("Commands").withStyle(ChatFormatting.WHITE));
        return header.append(Component.literal("  $name or /" + root + " name").withStyle(ChatFormatting.DARK_GRAY));
    }

    public static List<MutableComponent> entries(CommandRegistry registry) {
        List<MutableComponent> entries = new ArrayList<>();
        for (CommandDefinition definition : registry.all()) {
            entries.add(entry(definition));
        }
        return entries;
    }

    private static MutableComponent entry(CommandDefinition definition) {
        MutableComponent line = Component.literal("  ");
        line.append(Component.literal(definition.name()).withStyle(ChatFormatting.WHITE));
        line.append(Component.literal(" - " + definition.description()).withStyle(ChatFormatting.GRAY));
        return line.setStyle(line.getStyle()
                .withHoverEvent(new HoverEvent.ShowText(details(definition)))
                .withClickEvent(new ClickEvent.SuggestCommand("$" + definition.name())));
    }

    private static Component details(CommandDefinition definition) {
        MutableComponent detail = Component.literal(definition.usage()).withStyle(ChatFormatting.WHITE);
        if (!definition.aliases().isEmpty()) {
            detail.append(Component.literal("  " + String.join(", ", definition.aliases()))
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
        for (ArgumentDefinition argument : definition.arguments()) {
            detail.append(Component.literal("\n" + argument.describe()).withStyle(ChatFormatting.GRAY));
        }
        detail.append(Component.literal("\nClick to fill the command").withStyle(ChatFormatting.DARK_GRAY));
        return detail;
    }
}
