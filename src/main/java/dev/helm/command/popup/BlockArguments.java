package dev.helm.command.popup;

import java.util.ArrayList;
import java.util.List;

public final class BlockArguments {

    private BlockArguments() {
    }

    public static boolean atBlockName(List<String> arguments) {
        return !arguments.isEmpty() && !isCount(last(arguments));
    }

    public static boolean typing(List<String> arguments, String partial) {
        return atBlockName(arguments) && !partial.isEmpty();
    }

    public static List<String> alreadyNamed(List<String> arguments) {
        List<String> named = new ArrayList<>();
        for (int index = 0; index < arguments.size(); index++) {
            String token = arguments.get(index);
            if (!isCount(token)) {
                named.add(blockOf(token));
            }
        }
        return named;
    }

    private static String last(List<String> arguments) {
        return arguments.get(arguments.size() - 1);
    }

    private static String blockOf(String token) {
        int opening = token.indexOf('[');
        return opening < 0 ? token : token.substring(0, opening);
    }

    private static boolean isCount(String token) {
        if (token.isEmpty()) {
            return false;
        }
        for (int index = 0; index < token.length(); index++) {
            if (!Character.isDigit(token.charAt(index))) {
                return false;
            }
        }
        return true;
    }
}