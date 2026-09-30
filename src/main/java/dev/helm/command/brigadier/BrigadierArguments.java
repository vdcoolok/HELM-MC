package dev.helm.command.brigadier;

import java.util.Optional;

import com.mojang.brigadier.context.CommandContext;

import dev.helm.command.ArgumentAccess;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

public final class BrigadierArguments implements ArgumentAccess {

    private final CommandContext<FabricClientCommandSource> context;

    public BrigadierArguments(CommandContext<FabricClientCommandSource> context) {
        this.context = context;
    }

    @Override
    public boolean has(String name) {
        try {
            context.getArgument(name, Object.class);
            return true;
        } catch (IllegalArgumentException absent) {
            return false;
        }
    }

    @Override
    public Optional<String> string(String name) {
        return has(name) ? Optional.of(String.valueOf(context.getArgument(name, String.class))) : Optional.empty();
    }

    @Override
    public Optional<Integer> integer(String name) {
        return has(name) ? Optional.of(context.getArgument(name, Integer.class)) : Optional.empty();
    }

    @Override
    public Optional<Double> decimal(String name) {
        return has(name) ? Optional.of(context.getArgument(name, Double.class)) : Optional.empty();
    }

    @Override
    public Optional<Boolean> bool(String name) {
        return has(name) ? Optional.of(context.getArgument(name, Boolean.class)) : Optional.empty();
    }
}
