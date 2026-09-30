package dev.helm.command.brigadier;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;

import dev.helm.command.CommandOutput;

public final class SourceOutput implements CommandOutput {

    private final FabricClientCommandSource source;

    public SourceOutput(FabricClientCommandSource source) {
        this.source = source;
    }

    @Override
    public void feedback(Component message) {
        source.sendFeedback(message);
    }

    @Override
    public void error(Component message) {
        source.sendError(message);
    }
}
