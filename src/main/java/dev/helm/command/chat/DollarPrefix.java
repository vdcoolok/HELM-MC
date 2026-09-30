package dev.helm.command.chat;

public final class DollarPrefix {

    public static final char PREFIX = '$';
    public static final int PREFIX_LENGTH = 1;

    private DollarPrefix() {
    }

    public static boolean isCommand(String message) {
        return !message.isEmpty() && message.charAt(0) == PREFIX;
    }

    public static String body(String message) {
        return message.substring(1);
    }
}
