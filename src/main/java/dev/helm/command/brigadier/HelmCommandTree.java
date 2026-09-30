package dev.helm.command.brigadier;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import dev.helm.command.ArgumentDefinition;
import dev.helm.command.ArgumentType;
import dev.helm.command.CommandCall;
import dev.helm.command.CommandDefinition;
import dev.helm.command.CommandRegistry;
import dev.helm.command.CommandResult;
import dev.helm.command.CommandRoot;
import dev.helm.command.help.CommandHelp;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

public final class HelmCommandTree {

    private HelmCommandTree() {
    }

    public static LiteralArgumentBuilder<FabricClientCommandSource> build(CommandRegistry registry, String root) {
        LiteralArgumentBuilder<FabricClientCommandSource> node =
                LiteralArgumentBuilder.<FabricClientCommandSource>literal(root)
                        .executes(context -> help(context, registry));
        for (CommandDefinition definition : registry.all()) {
            node.then(buildCommand(registry, definition, definition.name()));
            for (String alias : definition.aliases()) {
                node.then(buildCommand(registry, definition, alias));
            }
        }
        return node;
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> buildCommand(CommandRegistry registry,
                                                                                  CommandDefinition definition,
                                                                                  String label) {
        List<ArgumentDefinition> required = definition.arguments().stream()
                .filter(ArgumentDefinition::required)
                .toList();
        Optional<ArgumentDefinition> optional = definition.arguments().stream()
                .filter(argument -> !argument.required())
                .findFirst();

        LiteralArgumentBuilder<FabricClientCommandSource> command =
                LiteralArgumentBuilder.<FabricClientCommandSource>literal(label);

        if (required.isEmpty()) {
            command.executes(context -> execute(registry, definition, label, context));
        }

        RequiredArgumentBuilder<FabricClientCommandSource, Object> chain = null;
        for (ArgumentDefinition argument : required) {
            RequiredArgumentBuilder<FabricClientCommandSource, Object> branch = argumentNode(argument);
            if (chain == null) {
                command.then(branch);
            } else {
                chain.then(branch);
            }
            chain = branch;
        }
        if (chain != null) {
            chain.executes(context -> execute(registry, definition, label, context));
        }

        optional.ifPresent(argument -> command.then(argumentNode(argument)
                .executes(context -> execute(registry, definition, label, context))));

        return command;
    }

    private static RequiredArgumentBuilder<FabricClientCommandSource, Object> argumentNode(ArgumentDefinition argument) {
        return RequiredArgumentBuilder
                .<FabricClientCommandSource, Object>argument(argument.name(),
                        BrigadierArgumentTypes.forArgument(argument))
                .suggests((context, builder) -> suggest(argument, builder));
    }

    private static int execute(CommandRegistry registry, CommandDefinition definition, String label,
                               CommandContext<FabricClientCommandSource> context) {
        CommandCall call = new CommandCall(definition, registry, label,
                new BrigadierArguments(context), new SourceOutput(context.getSource()));
        return definition.executor().execute(call).status();
    }

    private static int help(CommandContext<FabricClientCommandSource> context, CommandRegistry registry) {
        CommandHelp.send(registry, CommandRoot.NAME, new SourceOutput(context.getSource()));
        return CommandResult.SUCCESS.status();
    }

    private static CompletableFuture<Suggestions> suggest(ArgumentDefinition argument, SuggestionsBuilder builder) {
        if (argument.type() == ArgumentType.BOOLEAN) {
            for (String value : List.of("true", "false")) {
                builder.suggest(value);
            }
        }
        return builder.buildFuture();
    }
}
