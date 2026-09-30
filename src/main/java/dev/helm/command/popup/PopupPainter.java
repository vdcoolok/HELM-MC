package dev.helm.command.popup;

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
        for (int column = 1; column < starts.length; column++) {
            int x = starts[column] - PopupTheme.GAP / 2;
            graphics.fill(x, placement.y(), x + 1, placement.y() + placement.height(),
                    PopupTheme.EDGE);
        }
    }
}