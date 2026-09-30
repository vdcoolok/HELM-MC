package dev.helm.macro;

import java.util.ArrayList;
import java.util.List;

public final class MacroParser {

    private static final String WAIT = "wait";
    private static final String LOOP = "loop";
    private static final String END_LOOP = "endloop";
    private static final String GOTO = "goto";
    private static final String LOOKAT = "lookat";
    private static final String LOOKAT_HERE = "lookathere";
    private static final String GOTO_HERE = "gotohere";
    private static final String PRESS = "press";
    private static final String HOLD = "hold";
    private static final String RELEASE = "release";

    private MacroParser() {
    }

    public static MacroDocument parse(String name, List<String> source) {
        MacroLines lines = new MacroLines(MacroLine.read(source));
        List<MacroStatement> statements = block(lines, false);
        if (lines.hasNext()) {
            throw new MacroSyntaxException(unexpectedEndLoop(lines.peek()));
        }
        return new MacroDocument(name, statements);
    }

    private static List<MacroStatement> block(MacroLines lines, boolean insideLoop) {
        List<MacroStatement> statements = new ArrayList<>();

        while (lines.hasNext()) {
            MacroLine line = lines.peek();
            if (line.keyword().equals(END_LOOP)) {
                if (!insideLoop) {
                    throw new MacroSyntaxException(unexpectedEndLoop(line));
                }
                return statements;
            }
            statements.add(statement(lines));
        }

        if (insideLoop) {
            throw new MacroSyntaxException("'" + LOOP + "' is missing '" + END_LOOP + "'");
        }
        return statements;
    }

    private static MacroStatement statement(MacroLines lines) {
        MacroLine line = lines.next();
        return switch (line.keyword()) {
            case WAIT -> new MacroStatement.Wait(MacroDuration.parse(single(line, "duration")));
            case LOOP -> loop(line, lines);
            case GOTO -> move(line);
            case GOTO_HERE -> here(line, new MacroStatement.MoveHere());
            case LOOKAT -> look(line);
            case LOOKAT_HERE -> here(line, new MacroStatement.LookHere());
            case HOLD -> new MacroStatement.Hold(input(line));
            case RELEASE -> new MacroStatement.Release(input(line));
            case PRESS -> new MacroStatement.Press(input(line));
            default -> throw new MacroSyntaxException(
                    "Unknown command '" + line.keyword() + "' on line " + line.number());
        };
    }

    private static MacroStatement loop(MacroLine head, MacroLines lines) {
        long repeats = head.hasArguments() ? MacroCount.parse(head.argument(0), head.number()) : -1L;
        List<MacroStatement> body = block(lines, true);
        lines.expectEndLoop();
        return new MacroStatement.Loop(repeats, body);
    }

    private static <T extends MacroStatement> T here(MacroLine line, T statement) {
        requireNoArguments(line, statement.keyword());
        return statement;
    }

    private static void requireNoArguments(MacroLine line, String keyword) {
        if (line.hasArguments()) {
            throw new MacroSyntaxException("'" + keyword + "' takes no arguments, on line " + line.number());
        }
    }

    private static MacroStatement move(MacroLine line) {
        requireArgumentCount(line, 3);
        return new MacroStatement.Move(number(line, 0, "x"), number(line, 1, "y"), number(line, 2, "z"));
    }

    private static MacroStatement look(MacroLine line) {
        if (!line.hasArguments()) {
            throw new MacroSyntaxException("Missing angles on line " + line.number() + ". Use: lookat <pitch> / <yaw>");
        }
        return new MacroStatement.Look(MacroAngles.parse(line.joinArguments(), line.number()));
    }

    private static dev.helm.input.InputBinding input(MacroLine line) {
        String token = single(line, "input");
        return dev.helm.input.InputNames.find(token)
                .orElseThrow(() -> new MacroSyntaxException("Unknown input: " + token
                        + " on line " + line.number()));
    }

    private static double number(MacroLine line, int index, String axis) {
        String token = line.argument(index);
        try {
            return Double.parseDouble(token);
        } catch (NumberFormatException invalid) {
            throw new MacroSyntaxException("Invalid " + axis + ": " + token + " on line " + line.number());
        }
    }

    private static String single(MacroLine line, String subject) {
        if (line.arguments().size() != 1) {
            throw new MacroSyntaxException("Expected one " + subject + " on line " + line.number()
                    + " but found " + line.arguments().size());
        }
        return line.argument(0);
    }

    private static void requireArgumentCount(MacroLine line, int expected) {
        if (line.arguments().size() != expected) {
            throw new MacroSyntaxException("Expected " + expected + " arguments on line " + line.number()
                    + " but found " + line.arguments().size());
        }
    }

    private static String unexpectedEndLoop(MacroLine line) {
        return "'" + END_LOOP + "' without '" + LOOP + "' on line " + line.number();
    }
}
