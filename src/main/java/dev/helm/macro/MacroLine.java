package dev.helm.macro;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

record MacroLine(int number, String keyword, List<String> arguments) {

    static final char COMMENT = '#';

    static List<MacroLine> read(List<String> source) {
        List<MacroLine> lines = new ArrayList<>();
        for (int index = 0; index < source.size(); index++) {
            readLine(source.get(index), index + 1).ifPresent(lines::add);
        }
        return lines;
    }

    private static java.util.Optional<MacroLine> readLine(String raw, int number) {
        String line = raw == null ? "" : raw;
        int comment = line.indexOf(COMMENT);
        if (comment >= 0) {
            line = line.substring(0, comment);
        }
        line = line.trim();
        if (line.isEmpty()) {
            return java.util.Optional.empty();
        }

        String[] tokens = line.split("\\s+");
        String keyword = tokens[0].toLowerCase(Locale.ROOT);
        List<String> arguments = List.of(tokens).subList(1, tokens.length);
        return java.util.Optional.of(new MacroLine(number, keyword, arguments));
    }

    boolean hasArguments() {
        return !arguments.isEmpty();
    }

    String argument(int index) {
        if (index >= arguments.size()) {
            throw new MacroSyntaxException("Missing argument on line " + number);
        }
        return arguments.get(index);
    }

    String joinArguments() {
        return String.join(" ", arguments);
    }
}
