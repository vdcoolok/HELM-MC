package dev.helm.command;

import java.util.List;
import java.util.Locale;

import com.mojang.brigadier.context.StringRange;

import dev.helm.command.chat.LineTokenizer;

public final class ChatInput {

    private static final int BODY_START = 1;

    private final List<String> completed;
    private final String partial;
    private final int tokenStart;
    private final int cursor;

    private ChatInput(List<String> completed, String partial, int tokenStart, int cursor) {
        this.completed = completed;
        this.partial = partial;
        this.tokenStart = tokenStart;
        this.cursor = cursor;
    }

    public static ChatInput of(String body, int cursor) {
        int bounded = Math.min(Math.max(cursor, 0), body.length());
        int start = tokenStart(body, bounded);
        List<String> completed = LineTokenizer.tokenize(body.substring(0, start));
        String partial = bounded > start ? body.substring(start, bounded) : "";
        return new ChatInput(completed, partial, start, bounded);
    }

    public List<String> completed() {
        return completed;
    }

    public String partial() {
        return partial;
    }

    public String lowerPartial() {
        return partial.toLowerCase(Locale.ROOT);
    }

    public StringRange range() {
        return new StringRange(BODY_START + tokenStart, BODY_START + cursor);
    }

    public boolean atTokenStart() {
        return tokenStart == cursor;
    }

    private static int tokenStart(String text, int cursor) {
        int index = cursor;
        while (index > 0 && !Character.isWhitespace(text.charAt(index - 1))) {
            index--;
        }
        return index;
    }
}
