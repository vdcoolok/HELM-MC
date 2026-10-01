package dev.helm.command.popup;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class PopupPainter {

    private PopupPainter() {
    }

    public static void paint(GuiGraphicsExtractor graphics, Font font, PopupPlacement placement,
                             int selected, int mouseX, int mouseY) {
        List<PopupRow> rows = placement.rows();
        if (rows.isEmpty()) {
            return;
        }

        int left = placement.x();
        int top = placement.y();
        int right = left + placement.width();
        int bottom = top + placement.height();

        graphics.fill(left, top, right, bottom, PopupTheme.BACKGROUND);
        outline(graphics, left, top, right, bottom);
        paintColumnEdges(graphics, placement);
        if (placement.scrolls()) {
            paintScrollMarks(graphics, placement, left, right, top, bottom);
        }

        int hovered = placement.rowAt(mouseX, mouseY);
        for (PopupPlacement.Cell cell : placement.cells()) {
            PopupRow row = rows.get(cell.row());
            boolean active = cell.row() == selected || cell.row() == hovered;

            int textY = cell.y() + PopupTheme.TEXT_TOP;
            graphics.text(font, row.label(), cell.x() + PopupTheme.TEXT_PAD, textY,
                    active ? PopupTheme.SELECTED : PopupTheme.TEXT);

            if (PopupRows.hintFits(row, font, cell.width() - PopupTheme.TEXT_PAD * 2)) {
                int hintX = cell.x() + cell.width() - PopupTheme.TEXT_PAD - font.width(row.note());
                graphics.text(font, row.note(), hintX, textY,
                        active ? PopupTheme.NOTE_SELECTED : PopupTheme.NOTE);
            }
        }

        if (placement.hasDetail()) {
            paintDetail(graphics, font, placement, rows, selected, hovered);
        }
    }

    private static void paintDetail(GuiGraphicsExtractor graphics, Font font,
                                    PopupPlacement placement, List<PopupRow> rows,
                                    int selected, int hovered) {
        int row = described(placement, selected, hovered);
        if (row < 0) {
            return;
        }
        List<String> lines = SettingPicker.details(placement.rows().get(row));
        if (lines.isEmpty()) {
            return;
        }

        int left = placement.detailX();
        int top = placement.y();
        int inner = placement.detailWidth() - PopupTheme.TEXT_PAD * 2;
        List<String> wrapped = new ArrayList<>();
        for (String line : lines) {
            wrapped.addAll(wrap(font, line, inner));
        }

        int limit = Math.max(1, (placement.height() - PopupTheme.PAD_Y * 2) / PopupTheme.DETAIL_LINE);
        int shown2 = Math.min(wrapped.size(), limit);

        int textY = top + PopupTheme.PAD_Y;
        for (int index = 0; index < shown2; index++) {
            String line = wrapped.get(index);
            int colour = index == 0
                    ? PopupTheme.SELECTED
                    : line.startsWith("currently")
                            ? PopupTheme.NOTE_SELECTED
                            : PopupTheme.NOTE;
            graphics.text(font, line, left + PopupTheme.TEXT_PAD, textY, colour);
            textY += PopupTheme.DETAIL_LINE;
        }
    }

    private static int described(PopupPlacement placement, int selected, int hovered) {
        List<PopupPlacement.Cell> cells = placement.cells();
        if (cells.isEmpty()) {
            return -1;
        }
        if (onScreen(cells, selected)) {
            return selected;
        }
        if (onScreen(cells, hovered)) {
            return hovered;
        }
        return cells.get(0).row();
    }

    private static boolean onScreen(List<PopupPlacement.Cell> cells, int row) {
        if (row < 0) {
            return false;
        }
        for (PopupPlacement.Cell cell : cells) {
            if (cell.row() == row) {
                return true;
            }
        }
        return false;
    }

    private static List<String> wrap(Font font, String line, int width) {
        List<String> out = new ArrayList<>();
        if (font.width(line) <= width) {
            out.add(line);
            return out;
        }
        StringBuilder current = new StringBuilder();
        for (String word : line.split(" ")) {
            String candidate = current.isEmpty() ? word : current + " " + word;
            if (font.width(candidate) <= width) {
                current.setLength(0);
                current.append(candidate);
                continue;
            }
            if (!current.isEmpty()) {
                out.add(current.toString());
                current.setLength(0);
            }
            String rest = word;
            while (font.width(rest) > width) {
                String head = font.plainSubstrByWidth(rest, width);
                if (head.isEmpty()) {
                    break;
                }
                out.add(head);
                rest = rest.substring(head.length());
            }
            current.append(rest);
        }
        if (!current.isEmpty()) {
            out.add(current.toString());
        }
        return out;
    }

    private static void paintScrollMarks(GuiGraphicsExtractor graphics, PopupPlacement placement,
                                         int left, int right, int top, int bottom) {
        if (!placement.atTop()) {
            stripe(graphics, left, right, top + 1, PopupTheme.SCROLL_MARK);
        }
        if (!placement.atBottom()) {
            stripe(graphics, left, right, bottom - 2, PopupTheme.SCROLL_MARK);
        }
    }

    private static void stripe(GuiGraphicsExtractor graphics, int left, int right, int y,
                               int colour) {
        for (int x = left + 1; x < right - 1; x += 2) {
            graphics.fill(x, y, x + 1, y + 1, colour);
        }
    }

    private static void outline(GuiGraphicsExtractor graphics, int left, int top, int right,
                                int bottom) {
        graphics.fill(left, top, right, top + 1, PopupTheme.EDGE);
        graphics.fill(left, bottom - 1, right, bottom, PopupTheme.EDGE);
        graphics.fill(left, top, left + 1, bottom, PopupTheme.EDGE);
        graphics.fill(right - 1, top, right, bottom, PopupTheme.EDGE);
    }

    private static void paintColumnEdges(GuiGraphicsExtractor graphics, PopupPlacement placement) {
        int[] starts = placement.columnX();
        int last = starts.length - 1;
        for (int column = 1; column <= last; column++) {
            int x = column == last && placement.hasDetail()
                    ? placement.detailX() - PopupTheme.GAP / 2
                    : starts[column] - PopupTheme.GAP / 2;
            graphics.fill(x, placement.y(), x + 1, placement.y() + placement.height(),
                    PopupTheme.EDGE);
        }
    }
}