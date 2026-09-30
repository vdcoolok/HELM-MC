package dev.helm.command.help;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import dev.helm.command.ArgumentDefinition;
import dev.helm.command.CommandDefinition;
import dev.helm.command.CommandFeedback;
import dev.helm.command.CommandRegistry;

public final class CommandHelp {

    private CommandHelp() {
    }

    public static Component render(CommandRegistry registry, String root) {
        MutableComponent help = Component.literal(CommandFeedback.PREFIX)
                .withStyle(ChatFormatting.DARK_AQUA);
        help.append(Component.literal("Commands").withStyle(ChatFormatting.AQUA));
        help.append(Component.literal("  (use $name or /" + root + " name)").withStyle(ChatFormatting.DARK_GRAY));

        for (CommandDefinition definition : registry.all()) {
            help.append(usageLine(definition));
            help.append(descriptionLine(definition));
        }
        return help;
    }

    private static MutableComponent usageLine(CommandDefinition definition) {
        MutableComponent line = Component.literal("  ");
        line.append(Component.literal("$" + definition.usage()).withStyle(ChatFormatting.WHITE));
        return line;
    }

    private static MutableComponent descriptionLine(CommandDefinition definition) {
        String description = definition.description();
        if (!definition.aliases().isEmpty()) {
            description = description + "  (aliases: " + String.join(", ", definition.aliases()) + ")";
        }
        if (!definition.arguments().isEmpty()) {
            description = description + "  "
                    + definition.arguments().stream().map(ArgumentDefinition::describe)
                    .reduce((left, right) -> left + ", " + right)
                    .orElse("");
        }
        return Component.literal("      " + description).withStyle(ChatFormatting.GRAY);
    }
}
