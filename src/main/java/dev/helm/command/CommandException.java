package dev.helm.command;

import java.util.Objects;

import net.minecraft.network.chat.Component;

public class CommandException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final transient Component message;

    public CommandException(String message) {
        super(message);
        this.message = Component.literal(message);
    }

    public CommandException(Component message) {
        super(message.getString());
        this.message = Objects.requireNonNull(message);
    }

    public Component message() {
        return message;
    }
}
