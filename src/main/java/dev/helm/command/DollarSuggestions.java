package dev.helm.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;

public final class DollarSuggestions {

    private static final int BODY_START = 1;

    private DollarSuggestions() {
    }

    public static Suggestions build(CommandRegistry registry, String input, int cursor) {
        int bounded = Math.min(Math.max(cursor, BODY_START), input.length());
        int start = tokenStart(input, bounded);
        StringRange empty = new StringRange(start, start);

        if (startAfterCommandName(registry, input, start)) {
            return new Suggestions(empty, List.of());
        }

        StringRange range = new StringRange(start, bounded);
        String partial = input.substring(start, bounded).toLowerCase(Locale.ROOT);

        List<Suggestion> suggestions = new ArrayList<>();
        for (CommandDefinition definition : registry.all()) {
            if (definition.name().startsWith(partial)) {
                suggestions.add(new Suggestion(range, definition.name()));
            }
        }
        return new Suggestions(range, suggestions);
    }

    private static int tokenStart(String input, int cursor) {
        int index = cursor;
        while (index > BODY_START && !Character.isWhitespace(input.charAt(index - 1))) {
            index--;
        }
        return Math.max(index, BODY_START);
    }

    private static boolean startAfterCommandName(CommandRegistry registry, String input, int start) {
        if (start <= BODY_START) {
            return false;
        }
        if (start < input.length() && !Character.isWhitespace(input.charAt(start))) {
            return false;
        }
        String label = input.substring(BODY_START, start).trim();
        return registry.find(label).map(definition -> definition.arguments().isEmpty()).orElse(false);
    }
}
