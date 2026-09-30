package dev.helm.command;

import java.util.List;

public interface CommandExecutor {

    CommandResult execute(CommandCall call);
}
