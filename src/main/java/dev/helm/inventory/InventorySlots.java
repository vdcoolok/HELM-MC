package dev.helm.inventory;

public final class InventorySlots {

    public static final int HOTBAR = 9;
    public static final int CARRIED = 36;

    public static final int FIRST_PACKED = HOTBAR;
    public static final int TOOL = 0;
    public static final int PLACEMENT = 8;

    private InventorySlots() {
    }

    public static boolean onHotbar(int slot) {
        return slot >= 0 && slot < HOTBAR;
    }

    public static boolean packed(int slot) {
        return slot >= FIRST_PACKED && slot < CARRIED;
    }

    public static boolean carried(int slot) {
        return slot >= 0 && slot < CARRIED;
    }
}