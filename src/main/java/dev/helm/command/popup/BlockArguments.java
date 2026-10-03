package dev.helm.command.popup;

import java.util.ArrayList;
import java.util.List;

public final class BlockArguments {

    private BlockArguments() {
    }

    public static boolean atBlockName(List<String> arguments) {
        if (arguments.isEmpty()) {
            return false;
        }
        if (arguments.size() > 1) {
            return true;
        }
        return !isCount(arguments.get(0));
    }

    public static boolean typing(List<String> arguments, String partial) {
        return atBlockName(arguments) && !partial.isEmpty();
    }

    public static List<String> alreadyNamed(List<String> arguments) {
        int first = !arguments.isEmpty() && isCount(arguments.get(0)) ? 1 : 0;
        List<String> named = new ArrayList<>();
        for (int index = first; index < arguments.size(); index++) {
            named.add(blockOf(arguments.get(index)));
        }
        return named;
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