package dev.helm.command;

public enum CommandResult {

    SUCCESS,
    FAILURE;

    public int status() {
        return this == SUCCESS ? 1 : 0;
    }
}
