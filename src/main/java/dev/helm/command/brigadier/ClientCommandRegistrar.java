package dev.helm.command.brigadier;

import com.mojang.brigadier.CommandDispatcher;

import dev.helm.command.CommandRegistry;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

public final class ClientCommandRegistrar {

    private ClientCommandRegistrar() {
    }

    public static void register(String root, CommandRegistry registry) {
        ClientCommandRegistrationCallback.EVENT.register(
                (dispatcher, buildContext) -> install(dispatcher, root, registry));
    }

    private static void install(CommandDispatcher<FabricClientCommandSource> dispatcher,
                                String root,
                                CommandRegistry registry) {
        dispatcher.register(HelmCommandTree.build(registry, root));
    }
}
