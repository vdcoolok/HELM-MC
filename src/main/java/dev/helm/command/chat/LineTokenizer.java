package dev.helm.command.chat;

import java.util.ArrayList;
import java.util.List;

public final class LineTokenizer {

    private LineTokenizer() {
    }

    public static List<String> tokenize(String line) {
        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        boolean started = false;

        for (int index = 0; index < line.length(); index++) {
            char character = line.charAt(index);

            if (character == '\\' && index + 1 < line.length()) {
                current.append(line.charAt(index + 1));
                started = true;
                index++;
                continue;
            }
            if (character == '"') {
                inQuotes = !inQuotes;
                started = true;
                continue;
            }
            if (Character.isWhitespace(character) && !inQuotes) {
                if (started) {
                    tokens.add(current.toString());
                    current.setLength(0);
                    started = false;
                }
                continue;
            }
            current.append(character);
            started = true;
        }

        if (started) {
            tokens.add(current.toString());
        }
        return tokens;
    }
}
