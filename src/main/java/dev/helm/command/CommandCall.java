package dev.helm.command;

public record CommandCall(Command command, ArgumentAccess arguments, CommandOutput output) {

    public String label() {
        return command.path();
    }
}
