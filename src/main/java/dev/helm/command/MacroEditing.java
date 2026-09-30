package dev.helm.command;

import java.util.ArrayList;
import java.util.List;

import dev.helm.command.builtin.ExitEditModeCommand;
import dev.helm.macro.MacroSyntax;
import dev.helm.macro.MacroSyntaxException;
import dev.helm.macro.edit.MacroEditor;

public final class MacroEditing {

    private MacroEditing() {
    }

    public static boolean isActive() {
        return MacroEditor.instance().isEditing();
    }

    public static CommandResult append(String line, CommandOutput output) {
        try {
            MacroEditor.instance().append(line);
        } catch (MacroSyntaxException invalid) {
            output.error(CommandFeedback.error(invalid.getMessage()));
            return CommandResult.FAILURE;
        } catch (IllegalStateException noTarget) {
            output.error(CommandFeedback.error(noTarget.getMessage()));
            return CommandResult.FAILURE;
        } catch (RuntimeException unwritable) {
            output.error(CommandFeedback.error("Could not write the macro"));
            return CommandResult.FAILURE;
        }

        output.feedback(CommandFeedback.success("Added: " + line));
        MacroEditor.instance().problem()
                .ifPresent(problem -> output.feedback(CommandFeedback.info("Not runnable yet: " + problem)));
        return CommandResult.SUCCESS;
    }

    public static List<String> keywords() {
        List<String> labels = new ArrayList<>();
        for (MacroSyntax.Entry entry : MacroSyntax.entries()) {
            labels.add(entry.keyword());
        }
        labels.add(ExitEditModeCommand.NAME);
        return labels;
    }
}