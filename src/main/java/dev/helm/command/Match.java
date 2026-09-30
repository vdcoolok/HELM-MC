package dev.helm.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record Match(Command command, List<String> arguments) {

    public String path() {
        return command.path();
    }
}
