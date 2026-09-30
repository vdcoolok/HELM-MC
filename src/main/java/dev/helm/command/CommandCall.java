package dev.helm.command;

public record CommandCall(CommandDefinition definition,
                           CommandRegistry registry,
                           String label,
                           ArgumentAccess arguments,
                           CommandOutput output) {
}
