package dev.helm.command.popup;

public record PopupRow(String label, String hint, int column) {

    public static final int SYNTAX_COLUMN = 0;
    public static final int UTILITY_COLUMN = 1;
    public static final String SEPARATOR = "- ";

    public PopupRow {
        label = label == null ? "" : label;
        hint = hint == null ? "" : hint;
    }

    public static PopupRow syntax(String label, String hint) {
        return new PopupRow(label, hint, SYNTAX_COLUMN);
    }

    public static PopupRow utility(String label, String hint) {
        return new PopupRow(label, hint, UTILITY_COLUMN);
    }

    public String note() {
        return hint.isEmpty() ? "" : SEPARATOR + hint;
    }
}