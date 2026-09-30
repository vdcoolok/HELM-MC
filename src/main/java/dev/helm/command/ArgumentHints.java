package dev.helm.command;

import java.util.ArrayList;
import java.util.List;

import dev.helm.input.InputBinding;
import dev.helm.input.InputNames;

public final class ArgumentHints {

    private static final String INPUT = "<input>";

    private ArgumentHints() {
    }

    public static String next(String line) {
        List<String> tokens = dev.helm.command.chat.LineTokenizer.tokenize(line);
        if (tokens.size() != 1) {
            return null;
        }
        List<String> arguments = argumentsFor(tokens.get(0));
        return arguments.isEmpty() ? null : String.join(" ", arguments);
    }

    public static boolean wantsInput(String line) {
        List<String> tokens = dev.helm.command.chat.LineTokenizer.tokenize(line);
        if (tokens.size() != 1) {
            return false;
        }
        List<String> arguments = argumentsFor(tokens.get(0));
        return arguments.size() == 1 && INPUT.equals(arguments.get(0));
    }

    public static List<String> argumentsFor(String head) {
        List<String> fromSyntax = syntaxArguments(head);
        if (!fromSyntax.isEmpty()) {
            return fromSyntax;
        }
        return EditorShortcuts.find(head).map(EditorShortcut::arguments).orElse(List.of());
    }

    public static List<String> syntaxArguments(String keyword) {
        for (dev.helm.macro.MacroSyntax.Entry entry : dev.helm.macro.MacroSyntax.entries()) {
            if (entry.keyword().equalsIgnoreCase(keyword)) {
                return scan(entry.usage());
            }
        }
        return List.of();
    }

    public static List<InputBinding> inputs() {
        return InputNames.all();
    }

    public static String describe(InputBinding binding) {
        return InputNames.describe(binding);
    }

    public static boolean isKeyboard(InputBinding binding) {
        return binding.isKeyboard();
    }

    public static List<String> labels(List<InputBinding> bindings) {
        List<String> labels = new ArrayList<>();
        for (InputBinding binding : bindings) {
            labels.add(binding.label());
        }
        return labels;
    }

    private static List<String> scan(String usage) {
        List<String> found = new ArrayList<>();
        java.util.regex.Matcher matcher =
                java.util.regex.Pattern.compile("<[^>]+>|\\[[^\\]]+\\]").matcher(usage);
        while (matcher.find()) {
            found.add(matcher.group());
        }
        return found;
    }
}