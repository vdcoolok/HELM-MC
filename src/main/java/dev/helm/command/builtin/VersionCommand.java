package dev.helm.command.builtin;

import java.util.List;

import dev.helm.command.CommandDefinition;
import dev.helm.command.CommandRegistry;
import dev.helm.command.CommandResult;
import net.fabricmc.loader.api.FabricLoader;

public final class VersionCommand {

    private static final String MOD_ID = "helm";

    private VersionCommand() {
    }

    public static void register(CommandRegistry registry) {
        registry.register(new CommandDefinition("version", List.of("ver"),
                "Shows the loaded HELM version.",
                List.of(),
                call -> {
                    call.output().feedback(dev.helm.command.CommandFeedback
                            .success("version " + resolveVersion()));
                    return CommandResult.SUCCESS;
                }));
    }

    public static String resolveVersion() {
        return FabricLoader.getInstance()
                .getModContainer(MOD_ID)
                .map(container -> container.getMetadata().getVersion().getFriendlyString())
                .orElse("unknown");
    }
}
