package dev.helm.command;

public final class CommandRunner {

    private CommandRunner() {
    }

    public static CommandResult run(Command command, ArgumentAccess arguments, CommandOutput output) {
        try {
            return command.executor().execute(new CommandCall(command, arguments, output));
        } catch (CommandException failure) {
            output.error(failure.message());
            return CommandResult.FAILURE;
        } catch (RuntimeException unexpected) {
            output.error(CommandFeedback.error(unexpected.getMessage()));
            return CommandResult.FAILURE;
        }
    }
}
