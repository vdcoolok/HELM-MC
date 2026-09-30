package dev.helm.command.builtin;

import dev.helm.command.Command;
import dev.helm.command.CommandCall;
import dev.helm.command.CommandFeedback;
import dev.helm.command.CommandResult;
import dev.helm.macro.edit.MacroEditor;

public final class ExitEditModeCommand {

    public static final String NAME = "exitEditMode";

    private ExitEditModeCommand() {
    }

    public static Command build() {
        return Command.leaf(NAME, ExitEditModeCommand::close)
                .also("exit", "stopEdit", "exitEdit")
                .describedAs("Closes the open macro.")
                .shownWhen(() -> MacroEditor.instance().isEditing());
    }

    public static CommandResult close(CommandCall call) {
        if (!MacroEditor.instance().isEditing()) {
            call.output().error(CommandFeedback.error("No macro is open for editing"));
            return CommandResult.FAILURE;
        }
        String name = MacroEditor.instance().target().orElse("macro");
        MacroEditor.instance().end();
        call.output().feedback(CommandFeedback.success("Closed " + name));
        return CommandResult.SUCCESS;
    }
}