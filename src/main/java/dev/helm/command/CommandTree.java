package dev.helm.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;

import dev.helm.command.chat.LineTokenizer;
import dev.helm.command.chat.TokenArguments;
import dev.helm.command.help.CommandHelp;
import dev.helm.macro.edit.MacroEditor;

public final class CommandTree {

    public record Position(Command command, int consumed) {
    }

    private static final CommandTree INSTANCE = new CommandTree();

    private final List<Command> roots = new ArrayList<>();

    private CommandTree() {
    }

    public static CommandTree instance() {
        return INSTANCE;
    }

    public void install(Command... commands) {
        roots.clear();
        roots.addAll(List.of(commands));
    }

    public List<Command> roots() {
        return List.copyOf(roots);
    }

    public CommandResult dispatch(String body, CommandOutput output) {
        String trimmed = body == null ? "" : body.trim();
        if (trimmed.isEmpty()) {
            help(output);
            return CommandResult.SUCCESS;
        }

        if (resolve(LineTokenizer.tokenize(trimmed)).isEmpty() && MacroEditing.isActive()) {
            String shortcut = EditorShortcuts.expand(trimmed);
            if (shortcut != null) {
                return execute(shortcut, output);
            }
        }
        return execute(trimmed, output);
    }

    private CommandResult execute(String trimmed, CommandOutput output) {
        List<String> tokens = LineTokenizer.tokenize(trimmed);
        Optional<Match> match = resolve(tokens);

        if (match.isEmpty()) {
            if (MacroEditor.instance().isEditing()) {
                return MacroEditing.append(trimmed, output);
            }
            output.error(CommandFeedback.unknownCommand(tokens.get(0)));
            return CommandResult.FAILURE;
        }

        Command command = match.get().command();
        if (command.isGroup()) {
            CommandHelp.sendGroup(command, output);
            return CommandResult.SUCCESS;
        }
        return CommandRunner.run(command,
                TokenArguments.resolve(command.arguments(), match.get().arguments()), output);
    }

    public Suggestions complete(String body, int cursor) {
        ChatInput input = ChatInput.of(body == null ? "" : body, cursor);
        StringRange range = input.range();
        String partial = input.lowerPartial();

        List<Suggestion> suggestions = new ArrayList<>();
        for (String label : editableLabels(input.completed())) {
            if (label.toLowerCase(Locale.ROOT).startsWith(partial)) {
                suggestions.add(new Suggestion(range, label));
            }
        }
        return new Suggestions(range, suggestions);
    }

    private List<String> editableLabels(List<String> completed) {
        if (!completed.isEmpty() || !MacroEditing.isActive()) {
            return labelsAfter(completed);
        }
        return MacroEditing.keywords();
    }

    public String placeholder(String body, int cursor) {
        ChatInput input = ChatInput.of(body == null ? "" : body, cursor);
        if (!input.atTokenStart()) {
            return null;
        }
        return placeholderAfter(input.completed()).orElse(null);
    }

    public void help(CommandOutput output) {
        dev.helm.command.help.CommandHelp.send(this, output);
    }

    public Optional<Match> resolve(List<String> tokens) {
        if (tokens.isEmpty()) {
            return Optional.empty();
        }
        Optional<Command> found = root(tokens.get(0));
        if (found.isEmpty()) {
            return Optional.empty();
        }

        Command command = found.get();
        List<String> arguments = new ArrayList<>(tokens.subList(1, tokens.size()));
        while (command.isGroup() && !arguments.isEmpty()) {
            Optional<Command> child = command.child(arguments.remove(0));
            if (child.isEmpty() || !child.get().isVisible()) {
                break;
            }
            command = child.get();
        }
        return Optional.of(new Match(command, arguments));
    }

    public Optional<Command> root(String token) {
        return roots.stream()
                .filter(command -> command.isVisible())
                .filter(command -> command.matches(token))
                .findFirst();
    }

    public List<String> labelsAfter(List<String> completed) {
        if (completed.isEmpty()) {
            return namesOf(roots);
        }
        Position position = walk(completed);
        if (position == null) {
            return List.of();
        }
        Command command = position.command();
        if (command.isGroup()) {
            return namesOf(command.children());
        }
        return argumentValues(command, completed.size() - position.consumed());
    }

    public Optional<String> placeholderAfter(List<String> completed) {
        if (completed.isEmpty()) {
            return Optional.empty();
        }
        Position position = walk(completed);
        if (position == null || position.command().isGroup()) {
            return Optional.empty();
        }
        List<ArgumentDefinition> arguments = position.command().arguments();
        int index = completed.size() - position.consumed();
        if (index < 0 || index >= arguments.size()) {
            return Optional.empty();
        }
        ArgumentDefinition argument = arguments.get(index);
        if (argument.hasSuggestions()) {
            return Optional.empty();
        }
        return Optional.of(argument.placeholder());
    }

    private List<String> argumentValues(Command command, int index) {
        List<ArgumentDefinition> arguments = command.arguments();
        if (index < 0 || index >= arguments.size()) {
            return List.of();
        }
        ArgumentDefinition argument = arguments.get(index);
        if (argument.hasSuggestions()) {
            return argument.suggestedValues();
        }
        return switch (argument.type()) {
            case BOOLEAN -> List.of("true", "false");
            default -> List.of();
        };
    }

    private static List<String> namesOf(List<Command> commands) {
        List<String> labels = new ArrayList<>();
        commands.stream().filter(Command::isVisible).forEach(command -> labels.add(command.name()));
        return labels;
    }

    private Position walk(List<String> tokens) {
        Optional<Command> found = root(tokens.get(0));
        if (found.isEmpty()) {
            return null;
        }
        Command command = found.get();
        int index = 1;
        while (command.isGroup() && index < tokens.size()) {
            Optional<Command> child = command.child(tokens.get(index));
            if (child.isEmpty()) {
                break;
            }
            if (!child.get().isVisible()) {
                return null;
            }
            command = child.get();
            index++;
        }
        return new Position(command, index);
    }
}
