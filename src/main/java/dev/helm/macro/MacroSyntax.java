package dev.helm.macro;

import java.util.List;

public final class MacroSyntax {

    public static final String WAIT = "wait";
    public static final String LOOP = "loop";
    public static final String END_LOOP = "endloop";
    public static final String GOTO = "goto";
    public static final String LOOKAT = "lookat";
    public static final String LOOKAT_HERE = "lookathere";
    public static final String GOTO_HERE = "gotohere";
    public static final String HOLD = "hold";
    public static final String RELEASE = "release";
    public static final String PRESS = "press";

    public record Entry(String keyword, String usage, String example, String meaning) {
        public String arguments() {
            if (!usage.startsWith(keyword)) {
                return "";
            }
            return usage.substring(keyword.length()).trim();
        }
    }

    private static final List<Entry> ENTRIES = List.of(
            new Entry(WAIT, "wait <duration>", "wait 1s", "pause for a time"),
            new Entry(GOTO, "goto <x> <y> <z>", "goto -50 -50 -50", "walk to a position"),
            new Entry(GOTO_HERE, "gotohere", "gotohere", "walk to what you are aiming at"),
            new Entry(LOOKAT, "lookat <pitch> / <yaw>", "lookat 14/240", "turn to an angle"),
            new Entry(LOOKAT_HERE, "lookathere", "lookathere", "turn to what you are aiming at"),
            new Entry(HOLD, "hold <input>", "hold M1", "press and keep an input down"),
            new Entry(RELEASE, "release <input>", "release M1", "let an input go"),
            new Entry(PRESS, "press <input>", "press M1", "tap an input once"),
            new Entry(LOOP, "loop [count]", "loop 3", "repeat a block until endloop"),
            new Entry(END_LOOP, "endloop", "endloop", "close a loop"));

    private MacroSyntax() {
    }

    public static List<Entry> entries() {
        return ENTRIES;
    }

    public static boolean isKeyword(String token) {
        String name = token.toLowerCase(java.util.Locale.ROOT);
        return ENTRIES.stream().anyMatch(entry -> entry.keyword().equals(name));
    }

    public static String describe() {
        StringBuilder text = new StringBuilder();
        for (Entry entry : ENTRIES) {
            if (text.length() > 0) {
                text.append(", ");
            }
            text.append(entry.keyword());
        }
        return text.toString();
    }

    public static List<String> catalog() {
        int width = ENTRIES.stream().mapToInt(entry -> entry.usage().length()).max().orElse(0);
        return ENTRIES.stream()
                .map(entry -> pad(entry.usage(), width) + "  " + entry.meaning()
                        + ", e.g. " + entry.example())
                .toList();
    }

    private static String pad(String text, int width) {
        return text.length() >= width ? text : text + " ".repeat(width - text.length());
    }
}
