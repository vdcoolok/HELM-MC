package dev.helm.command.help;

import java.util.ArrayList;
import java.util.List;

import dev.helm.command.ArgumentDefinition;
import dev.helm.command.Command;
import dev.helm.command.CommandFeedback;
import dev.helm.command.CommandOutput;
import dev.helm.command.CommandTheme;
import dev.helm.command.CommandTree;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;

public final class CommandHelp {

    private CommandHelp() {
    }

    public static void send(CommandTree tree, CommandOutput output) {
        output.feedback(CommandTheme.brand().append(
                Component.literal("Commands").withStyle(ChatFormatting.WHITE)));
        for (Command command : tree.roots()) {
            if (command.isVisible()) {
                output.feedback(entry(command, "$" + command.name()));
            }
        }
    }

    public static void sendGroup(Command group, CommandOutput output) {
        output.feedback(CommandTheme.brand().append(
                Component.literal(group.path()).withStyle(ChatFormatting.WHITE)));
        for (Command child : group.children()) {
            if (child.isVisible()) {
                output.feedback(entry(child, "$" + child.path()));
            }
        }
    }

    private static MutableComponent entry(Command command, String fill) {
        MutableComponent line = Component.literal("  ");
        line.append(Component.literal(command.name()).withStyle(ChatFormatting.WHITE));
        if (!command.description().isEmpty()) {
            line.append(Component.literal(" - " + command.description()).withStyle(ChatFormatting.GRAY));
        }
        return line.setStyle(line.getStyle()
                .withHoverEvent(new HoverEvent.ShowText(details(command)))
                .withClickEvent(new ClickEvent.SuggestCommand(fill)));
    }

    private static Component details(Command command) {
        MutableComponent detail = Component.literal(command.usage()).withStyle(ChatFormatting.WHITE);
        if (command.children().isEmpty()) {
            List<String> labels = command.labels();
            if (labels.size() > 1) {
                detail.append(Component.literal("  " + String.join(", ", labels.subList(1, labels.size())))
                        .withStyle(ChatFormatting.DARK_GRAY));
            }
        } else {
            for (Command child : command.children()) {
                if (child.isVisible()) {
                    detail.append(Component.literal("\n" + child.path()).withStyle(ChatFormatting.GRAY));
                }
            }
        }
        for (ArgumentDefinition argument : command.arguments()) {
            detail.append(Component.literal("\n" + argument.describe()).withStyle(ChatFormatting.GRAY));
        }
        detail.append(Component.literal("\nClick to fill the command").withStyle(ChatFormatting.DARK_GRAY));
        return detail;
    }
}
