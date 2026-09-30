package dev.helm.command.brigadier;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;

import dev.helm.command.ArgumentDefinition;

public final class BrigadierArgumentTypes {

    private BrigadierArgumentTypes() {
    }

    @SuppressWarnings("unchecked")
    public static ArgumentType<Object> forArgument(ArgumentDefinition definition) {
        return switch (definition.type()) {
            case STRING -> (ArgumentType<Object>) (ArgumentType<?>) StringArgumentType.greedyString();
            case INTEGER -> (ArgumentType<Object>) (ArgumentType<?>) IntegerArgumentType.integer();
            case DOUBLE -> (ArgumentType<Object>) (ArgumentType<?>) DoubleArgumentType.doubleArg();
            case BOOLEAN -> (ArgumentType<Object>) (ArgumentType<?>) BoolArgumentType.bool();
        };
    }
}
