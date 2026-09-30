package dev.helm.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;

public final class HelmCommands {

    public static final String ROOT = "helm";

    private static final int SUCCESS = 1;

    private HelmCommands() {
    }

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher,
                                 CommandBuildContext buildContext) {
        dispatcher.register(LiteralArgumentBuilder.<FabricClientCommandSource>literal(ROOT)
                .executes(context -> report(context.getSource()))
                .then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("version")
                        .executes(context -> report(context.getSource()))));
    }

    private static int report(FabricClientCommandSource source) {
        source.sendFeedback(Component.translatable("commands.helm.header", version())
                .withStyle(ChatFormatting.GRAY));
        return SUCCESS;
    }

    private static Component version() {
        String version = FabricLoader.getInstance()
                .getModContainer("helm")
                .map(container -> container.getMetadata().getVersion().getFriendlyString())
                .orElse("unknown");
        return Component.literal(version).withStyle(ChatFormatting.AQUA);
    }
}
