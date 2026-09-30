package dev.helm.command.builtin;

import dev.helm.command.Command;
import dev.helm.command.CommandResult;
import dev.helm.command.CommandFeedback;

public final class VersionCommand {

    private static final String MOD_ID = "helm";

    private VersionCommand() {
    }

    public static Command build() {
        return Command.leaf("version", call -> {
            call.output().feedback(CommandFeedback.success("version " + resolveVersion()));
            return CommandResult.SUCCESS;
        }).also("ver").describedAs("Shows the loaded HELM version.");
    }

    public static String resolveVersion() {
        return net.fabricmc.loader.api.FabricLoader.getInstance()
                .getModContainer(MOD_ID)
                .map(container -> container.getMetadata().getVersion().getFriendlyString())
                .orElse("unknown");
    }
}
