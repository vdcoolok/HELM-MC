package dev.helm.command.popup;

public record PopupRow(String label, String hint, int column, String insert) {

    public static final int SYNTAX_COLUMN = 0;
    public static final int UTILITY_COLUMN = 1;
    public static final String SEPARATOR = "- ";

    public PopupRow(String label, String hint, int column) {
        this(label, hint, column, "");
    }

    public PopupRow {
        label = label == null ? "" : label;
        hint = hint == null ? "" : hint;
        insert = insert == null ? "" : insert;
    }

    public static PopupRow syntax(String label, String hint) {
        return new PopupRow(label, hint, SYNTAX_COLUMN, "");
    }

    public static PopupRow utility(String label, String hint) {
        return new PopupRow(label, hint, UTILITY_COLUMN, "");
    }

    public String note() {
        if (hint.isEmpty()) {
            return "";
        }
        return SEPARATOR + hint;
    }
}