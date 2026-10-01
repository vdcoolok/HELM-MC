package dev.helm.command.popup;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.Font;

public final class PopupPlacement {

    public record Cell(int x, int y, int width, int row) {
    }

    private final List<PopupRow> rows;
    private final List<Cell> cells;
    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private final int[] columnX;
    private final int[] columnWidths;
    private final int offset;
    private final int totalLines;
    private final int visibleLines;
    private final int firstColumn;
    private final int totalColumns;
    private final int detailX;
    private final int detailWidth;

    private PopupPlacement(List<PopupRow> rows, List<Cell> cells, int x, int y, int width,
                           int height, int[] columnX, int[] columnWidths, int offset,
                           int totalLines, int visibleLines, int firstColumn,
                           int totalColumns, int detailX, int detailWidth) {
        this.rows = rows;
        this.cells = cells;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.columnX = columnX;
        this.columnWidths = columnWidths;
        this.offset = offset;
        this.totalLines = totalLines;
        this.visibleLines = visibleLines;
        this.firstColumn = firstColumn;
        this.totalColumns = totalColumns;
        this.detailX = detailX;
        this.detailWidth = detailWidth;
    }

    public static PopupPlacement of(List<PopupRow> rows, Font font, int inputX, int inputY,
                                    int screenWidth, int wantedColumns, int wantedOffset,
                                    int wantedColumnOffset, boolean wantsDetail) {
        List<Integer> occupied = PopupRows.occupiedColumns(rows);
        int perScreen = Math.max(1,
                PopupRows.columnsThatFit(rows, font, screenWidth - detailRoom(screenWidth, wantsDetail),
                        occupied.size()));
        int totalColumns = occupied.size();
        int firstColumn = Math.min(Math.max(wantedColumnOffset, 0), Math.max(totalColumns - 1, 0));
        int columns = Math.min(perScreen, totalColumns - firstColumn);

        int rowHeight = PopupTheme.ROW_HEIGHT;
        int totalLines = Math.max(PopupRows.heightOf(rows, occupied), 1);
        int room = inputY - PopupTheme.PADDING - PopupTheme.PAD_Y * 2;
        int visible = Math.max(1, Math.min(totalLines, room / rowHeight));
        visible = Math.min(visible, PopupTheme.MAX_ROWS);
        int offset = Math.min(Math.max(wantedOffset, 0), Math.max(totalLines - visible, 0));
        int height = visible * rowHeight + PopupTheme.PAD_Y * 2;

        int[] widths = new int[columns];
        int listWidth = 0;
        for (int slot = 0; slot < columns; slot++) {
            widths[slot] = PopupRows.widthOf(rows, font, occupied.get(firstColumn + slot));
            listWidth += widths[slot];
        }
        listWidth += PopupTheme.GAP * (columns - 1);

        int detail = wantsDetail ? detailWidth(screenWidth, listWidth) : 0;
        int total = listWidth + (detail > 0 ? PopupTheme.GAP + detail : 0);

        int x = fitX(inputX, total, screenWidth);
        int y = Math.max(PopupTheme.PADDING, inputY - height - PopupTheme.PADDING);

        int[] columnX = new int[columns];
        int running = x;
        for (int slot = 0; slot < columns; slot++) {
            columnX[slot] = running;
            running += widths[slot] + PopupTheme.GAP;
        }
        int detailX = x + listWidth + PopupTheme.GAP;

        List<Cell> cells = new ArrayList<>();
        int firstRow = y + PopupTheme.PAD_Y;
        for (int slot = 0; slot < columns; slot++) {
            int column = occupied.get(firstColumn + slot);
            int line = 0;
            for (int index = 0; index < rows.size(); index++) {
                if (rows.get(index).column() != column) {
                    continue;
                }
                if (line >= offset && line < offset + visible) {
                    cells.add(new Cell(columnX[slot], firstRow + (line - offset) * rowHeight,
                            widths[slot], index));
                }
                line++;
            }
        }

        return new PopupPlacement(List.copyOf(rows), List.copyOf(cells), x, y, total, height,
                columnX, widths, offset, totalLines, visible, firstColumn, totalColumns,
                detail > 0 ? detailX : 0, detail);
    }

    private static int detailWidth(int screenWidth, int listWidth) {
        int room = screenWidth - PopupTheme.PADDING * 2 - listWidth - PopupTheme.GAP;
        return Math.min(Math.max(room, PopupTheme.MIN_DETAIL), PopupTheme.MAX_DETAIL);
    }

    private static int detailRoom(int screenWidth, boolean wantsDetail) {
        return wantsDetail ? PopupTheme.MIN_DETAIL + PopupTheme.GAP : 0;
    }

    private static int fitX(int inputX, int width, int screenWidth) {
        int limit = screenWidth - PopupTheme.PADDING;
        int x = Math.min(inputX + PopupTheme.TEXT_PAD, limit - width);
        return Math.max(x, PopupTheme.PADDING);
    }

    public int rowAt(int mouseX, int mouseY) {
        for (Cell cell : cells) {
            if (mouseX >= cell.x() && mouseX < cell.x() + cell.width()
                    && mouseY >= cell.y() && mouseY < cell.y() + PopupTheme.ROW_HEIGHT) {
                return cell.row();
            }
        }
        return -1;
    }

    public List<PopupRow> rows() {
        return rows;
    }

    public List<Cell> cells() {
        return cells;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public int columns() {
        return columnX.length;
    }

    public int offset() {
        return offset;
    }

    public int totalLines() {
        return totalLines;
    }

    public int visibleLines() {
        return visibleLines;
    }

    public boolean scrolls() {
        return totalLines > visibleLines;
    }

    public int firstColumn() {
        return firstColumn;
    }

    public int totalColumns() {
        return totalColumns;
    }

    public boolean scrollsSideways() {
        return totalColumns > columns();
    }

    public int maxFirstColumn() {
        return Math.max(totalColumns - columns(), 0);
    }

    public boolean hasDetail() {
        return detailWidth > 0;
    }

    public int detailX() {
        return detailX;
    }

    public int detailWidth() {
        return detailWidth;
    }

    public boolean atTop() {
        return offset <= 0;
    }

    public boolean atBottom() {
        return offset >= totalLines - visibleLines;
    }

    public int[] columnX() {
        return columnX;
    }

    public int[] columnWidths() {
        return columnWidths;
    }
}