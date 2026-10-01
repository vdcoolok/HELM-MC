package dev.helm.command.builtin;

import java.util.List;

import dev.helm.command.ArgumentDefinition;
import dev.helm.command.ArgumentType;
import dev.helm.command.Command;
import dev.helm.command.CommandCall;
import dev.helm.command.CommandFeedback;
import dev.helm.command.CommandResult;
import dev.helm.command.MacroEditing;
import dev.helm.macro.MacroDocument;
import dev.helm.macro.MacroParser;
import dev.helm.macro.MacroSyntax;
import dev.helm.macro.edit.MacroEditor;
import dev.helm.macro.runtime.MacroController;
import dev.helm.storage.MacroStore;

public final class MacroCommand {

    private MacroCommand() {
    }

    public static Command build() {
        return Command.group("macro")
                .also("macros")
                .describedAs("Creates, edits, loads, stops and lists macros.")
                .containing(
                        Command.leaf("create", MacroCommand::create)
                                .taking(ArgumentDefinition.required(
                                        "name", ArgumentType.STRING, "macro name"))
                                .describedAs("Makes a new empty macro."),
                        Command.leaf("edit", MacroCommand::edit)
                                .taking(ArgumentDefinition.optional(
                                        "name", ArgumentType.STRING, "macro name")
                                        .offering(MacroCommand::macroNames))
                                .describedAs("Opens a macro, or closes the open one."),
                        actionGroup(),
                        ExitEditModeCommand.build(),
                        Command.leaf("load", MacroCommand::load)
                                .taking(ArgumentDefinition.required(
                                        "name", ArgumentType.STRING, "macro name")
                                        .offering(MacroCommand::macroNames))
                                .describedAs("Starts a macro."),
                        Command.leaf("stop", call -> {
                            MacroController.instance().stop(call.output());
                            return CommandResult.SUCCESS;
                        }).describedAs("Stops the running macro."),
                        Command.leaf("list", MacroCommand::list).describedAs("Lists available macros."));
    }

    private static Command actionGroup() {
        return Command.group("action")
                .shownWhen(() -> MacroEditor.instance().isEditing())
                .describedAs("Manages the lines of the open macro.")
                .containing(
                        Command.leaf("add", MacroCommand::add).also("a")
                                .taking(ArgumentDefinition.optional("syntax",
                                        ArgumentType.STRING, "macro syntax"))
                                .describedAs("Adds a line to the open macro."),
                        Command.leaf("remove", call -> {
                            int line = call.arguments().requireInteger("line");
                            MacroEditor.instance().remove(line);
                            call.output().feedback(CommandFeedback.success("Removed line " + line));
                            return CommandResult.SUCCESS;
                        }).also("rm", "delete")
                                .taking(ArgumentDefinition.required("line",
                                        ArgumentType.INTEGER, "line number"))
                                .describedAs("Deletes a numbered line."),
                        Command.leaf("list", call -> {
                            List<String> lines = MacroEditor.instance().lines();
                            if (lines.isEmpty()) {
                                call.output().feedback(CommandFeedback.info("No lines"));
                                return CommandResult.SUCCESS;
                            }
                            call.output().feedback(CommandFeedback.info(lines.size() + " line(s)"));
                            for (int index = 0; index < lines.size(); index++) {
                                call.output().feedback(CommandFeedback.info("  " + (index + 1) + "  "
                                        + dev.helm.macro.MacroTarget.describe(lines.get(index))));
                            }
                            return CommandResult.SUCCESS;
                        }).describedAs("Shows the open macro as a numbered list."),
                        Command.leaf("move", call -> {
                            int from = call.arguments().requireInteger("from");
                            int to = call.arguments().requireInteger("to");
                            MacroEditor.instance().move(from, to);
                            call.output().feedback(CommandFeedback.success("Moved " + from + " to " + to));
                            return CommandResult.SUCCESS;
                        }).taking(
                                ArgumentDefinition.required("from",
                                        ArgumentType.INTEGER, "line to move"),
                                ArgumentDefinition.required("to",
                                        ArgumentType.INTEGER, "new position"))
                                .describedAs("Moves a line, shifting the rest."));
    }

    private static CommandResult add(CommandCall call) {
        if (!call.arguments().has("syntax")) {
            return catalog(call);
        }
        return MacroEditing.append(call.arguments().requireString("syntax"), call.output());
    }

    private static CommandResult catalog(CommandCall call) {
        call.output().feedback(CommandFeedback.info("macro syntax"));
        for (String line : MacroSyntax.catalog()) {
            call.output().feedback(CommandFeedback.info("  " + line));
        }
        call.output().feedback(CommandFeedback.info("add one with: macro action add <line>"));
        return CommandResult.SUCCESS;
    }

    private static CommandResult create(CommandCall call) {
        String name = call.arguments().requireString("name");
        MacroEditor.instance().create(name);
        call.output().feedback(CommandFeedback.success("Created macro: " + name));
        return CommandResult.SUCCESS;
    }

    private static CommandResult edit(CommandCall call) {
        if (!call.arguments().has("name")) {
            return ExitEditModeCommand.close(call);
        }
        String name = call.arguments().requireString("name");
        if (!MacroStore.exists(name)) {
            call.output().error(CommandFeedback.error("No macro named " + name));
            return CommandResult.FAILURE;
        }
        MacroEditor.instance().begin(name);
        call.output().feedback(CommandFeedback.success("Editing " + name
                + ". Type macro syntax directly to append, or macro action to manage lines."
                + " Close with exitEditMode."));
        return CommandResult.SUCCESS;
    }

    

    private static CommandResult load(CommandCall call) {
        String name = call.arguments().requireString("name");
        if (!MacroStore.exists(name)) {
            call.output().error(CommandFeedback.error("No macro named " + name));
            return CommandResult.FAILURE;
        }
        MacroDocument document = MacroParser.parse(name, MacroStore.read(name));
        MacroController.instance().start(document, call.output());
        return CommandResult.SUCCESS;
    }

    private static CommandResult list(CommandCall call) {
        List<String> names = macroNames();
        if (names.isEmpty()) {
            call.output().feedback(CommandFeedback.info("No macros in " + MacroStore.directory()));
            return CommandResult.SUCCESS;
        }
        call.output().feedback(CommandFeedback.info(names.size() + " macro(s) in "
                + MacroStore.directory().getFileName() + "/"));
        for (String name : names) {
            call.output().feedback(CommandFeedback.info("  " + name));
        }
        return CommandResult.SUCCESS;
    }

    static List<String> macroNames() {
        try {
            return MacroStore.list();
        } catch (RuntimeException unreadable) {
            return List.of();
        }
    }
}
