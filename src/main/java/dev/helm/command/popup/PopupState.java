package dev.helm.command.popup;

import java.util.List;

public final class PopupState {

    private static final PopupState INSTANCE = new PopupState();

    private List<PopupRow> rows = List.of();
    private int selected = -1;
    private int offset;
    private int columnOffset;

    private PopupState() {
    }

    public static PopupState instance() {
        return INSTANCE;
    }

    public boolean isOpen() {
        return !rows.isEmpty();
    }

    public List<PopupRow> rows() {
        return rows;
    }

    public int selected() {
        return selected;
    }

    public PopupRow current() {
        return selected < 0 || selected >= rows.size() ? null : rows.get(selected);
    }

    public int currentRow() {
        return selected;
    }

    public int selectedColumn() {
        PopupRow row = current();
        return row == null ? -1 : row.column();
    }

    public void open(List<PopupRow> offered) {
        rows = offered;
        selected = -1;
        offset = 0;
        columnOffset = 0;
    }

    public void close() {
        rows = List.of();
        selected = -1;
        offset = 0;
        columnOffset = 0;
    }

    public int offset() {
        return offset;
    }

    public int columnOffset() {
        return columnOffset;
    }

    public void columnOffset(int value) {
        columnOffset = Math.max(value, 0);
    }

    public boolean scrollColumnsBy(int delta, int maxOffset) {
        int limit = Math.max(maxOffset, 0);
        int next = Math.min(Math.max(columnOffset + delta, 0), limit);
        if (next == columnOffset) {
            return false;
        }
        columnOffset = next;
        return true;
    }

    public void offset(int value) {
        offset = Math.max(value, 0);
    }

    public boolean scrollBy(int delta, int maxOffset) {
        int limit = Math.max(maxOffset, 0);
        int next = Math.min(Math.max(offset + delta, 0), limit);
        if (next == offset) {
            return false;
        }
        offset = next;
        return true;
    }

    public void keepVisible(int visible, int total) {
        offset = Math.min(offset, Math.max(total - visible, 0));
    }

    public void select(int index) {
        if (index < 0 || index >= rows.size()) {
            return;
        }
        selected = index;
    }

    public boolean step(int delta) {
        if (rows.isEmpty()) {
            return false;
        }
        selected = Math.floorMod(selected + delta, rows.size());
        return true;
    }

    public boolean moveWithinColumn(int delta) {
        if (rows.isEmpty()) {
            return false;
        }
        List<Integer> sameColumn = indexesIn(rows.get(0).column());
        if (sameColumn.isEmpty()) {
            return step(1);
        }
        int position = sameColumn.indexOf(selected);
        int next = position < 0
                ? (delta > 0 ? 0 : sameColumn.size() - 1)
                : Math.floorMod(position + delta, sameColumn.size());
        selected = sameColumn.get(next);
        return true;
    }

    public int lineOf(int row) {
        int column = rows.get(row).column();
        int line = 0;
        for (int index = 0; index < rows.size(); index++) {
            if (rows.get(index).column() != column) {
                continue;
            }
            if (index == row) {
                return line;
            }
            line++;
        }
        return 0;
    }

    public void reveal(int row, int visible, int total) {
        int line = lineOf(row);
        if (line < offset) {
            offset = line;
        } else if (line >= offset + visible) {
            offset = line - visible + 1;
        }
        keepVisible(visible, total);
    }

    public boolean moveToColumn(int target) {
        if (rows.isEmpty() || target < 0 || target >= columns()) {
            return false;
        }
        List<Integer> wanted = indexesIn(target);
        if (wanted.isEmpty()) {
            return false;
        }
        selected = wanted.get(nearestOffset(wanted));
        return true;
    }

    public boolean selectSlot(int slot, int perScreen) {
        List<Integer> occupied = occupied();
        if (slot < 0 || slot >= occupied.size()) {
            return false;
        }
        if (perScreen > 0 && (slot < columnOffset || slot >= columnOffset + perScreen)) {
            columnOffset = Math.min(slot, Math.max(occupied.size() - perScreen, 0));
        }
        return moveToColumn(occupied.get(slot));
    }

    private int nearestOffset(List<Integer> wanted) {
        if (selected < 0) {
            return 0;
        }
        int best = 0;
        int bestDistance = Integer.MAX_VALUE;
        for (int position = 0; position < wanted.size(); position++) {
            int distance = Math.abs(wanted.get(position) - selected);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = position;
            }
        }
        return best;
    }

    public boolean moveToColumnSlot(int delta) {
        if (rows.isEmpty()) {
            return false;
        }
        List<Integer> occupied = occupied();
        int current = selectedColumn();
        int slot = occupied.indexOf(current);
        if (slot < 0) {
            slot = columnOffset;
        }
        int target = slot + delta;
        if (target < 0 || target >= occupied.size()) {
            return false;
        }
        columnOffset = target;
        return moveToColumn(occupied.get(target));
    }

    public List<Integer> occupied() {
        return PopupRows.occupiedColumns(rows);
    }

    public int columns() {
        return occupied().size();
    }

    private List<Integer> indexesIn(int column) {
        List<Integer> found = new java.util.ArrayList<>();
        for (int index = 0; index < rows.size(); index++) {
            if (rows.get(index).column() == column) {
                found.add(index);
            }
        }
        return found;
    }
}