package dev.helm.command.popup;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.minecraft.client.gui.Font;

public final class PopupRows {

    private PopupRows() {
    }

    public static List<PopupRow> build() {
        List<PopupRow> rows = new ArrayList<>();
        for (dev.helm.macro.MacroSyntax.Entry entry : dev.helm.macro.MacroSyntax.entries()) {
            rows.add(PopupRow.syntax(entry.keyword(), entry.arguments()));
        }
        for (dev.helm.command.EditorShortcut shortcut : dev.helm.command.EditorShortcuts.shown()) {
            rows.add(PopupRow.utility(shortcut.label(), shortcut.meaning()));
        }
        return rows;
    }

    public static List<PopupRow> filter(List<PopupRow> rows, String partial) {
        String prefix = partial == null ? "" : partial.toLowerCase(Locale.ROOT);
        List<PopupRow> matching = new ArrayList<>();
        for (PopupRow row : rows) {
            if (row.label().toLowerCase(Locale.ROOT).startsWith(prefix)) {
                matching.add(row);
            }
        }
        return matching;
    }

    public static List<PopupRow> inputs() {
        List<PopupRow> rows = new ArrayList<>();
        for (dev.helm.input.InputBinding binding : dev.helm.command.ArgumentHints.inputs()) {
            rows.add(new PopupRow(binding.label(), dev.helm.command.ArgumentHints.describe(binding),
                    binding.isKeyboard() ? 0 : 1));
        }
        return rows;
    }

    public static int heightOf(List<PopupRow> rows, List<Integer> occupied) {
        int most = 0;
        for (int column : occupied) {
            int count = 0;
            for (PopupRow row : rows) {
                if (row.column() == column) {
                    count++;
                }
            }
            most = Math.max(most, count);
        }
        return most;
    }

    public static int widthOf(List<PopupRow> rows, Font font, int column) {
        int width = 0;
        for (PopupRow row : rows) {
            if (row.column() == column) {
                width = Math.max(width, cellWidth(row, font));
            }
        }
        return width + PopupTheme.TEXT_PAD * 2;
    }

    public static int cellWidth(PopupRow row, Font font) {
        int label = font.width(row.label());
        int hint = row.note().isEmpty() ? 0 : font.width(row.note()) + PopupTheme.MIN_GAP;
        return label + hint;
    }

    public static int totalWidth(List<PopupRow> rows, Font font, int columns) {
        int total = 0;
        for (int column : occupiedColumns(rows)) {
            total += widthOf(rows, font, column);
        }
        return total + PopupTheme.GAP * (columns - 1);
    }

    public static List<Integer> occupiedColumns(List<PopupRow> rows) {
        List<Integer> occupied = new ArrayList<>();
        for (PopupRow row : rows) {
            if (!occupied.contains(row.column())) {
                occupied.add(row.column());
            }
        }
        if (occupied.isEmpty()) {
            occupied.add(0);
        }
        occupied.sort(Integer::compareTo);
        return occupied;
    }

    public static int columnsUsed(List<PopupRow> rows) {
        return occupiedColumns(rows).size();
    }

    public static int slotOf(List<Integer> occupied, int column) {
        int slot = occupied.indexOf(column);
        return slot < 0 ? 0 : slot;
    }

    public static int columnsThatFit(List<PopupRow> rows, Font font, int screenWidth,
                                     int wanted) {
        int room = screenWidth - PopupTheme.PADDING * 2;
        int usable = Math.min(wanted, columnsUsed(rows));
        for (int columns = usable; columns >= 1; columns--) {
            if (totalWidth(rows, font, columns) <= room) {
                return columns;
            }
        }
        return 1;
    }

    public static boolean hintFits(PopupRow row, Font font, int cellWidth) {
        if (row.note().isEmpty()) {
            return false;
        }
        return font.width(row.label()) + font.width(row.note()) + PopupTheme.MIN_GAP <= cellWidth;
    }
}