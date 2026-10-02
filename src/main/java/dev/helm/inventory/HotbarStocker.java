package dev.helm.inventory;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.block.Blocks;

import dev.helm.setting.Settings;
import dev.helm.tools.BestSlot;
import dev.helm.tools.ThrowawayChooser;
import dev.helm.world.PlayerInventory;

public final class HotbarStocker {

    private static final HotbarStocker INSTANCE = new HotbarStocker();

    private static final int TOOL_CHECK_TICKS = 20;

    private int inventoryTick;

    public static HotbarStocker instance() {
        return INSTANCE;
    }

    public void onTick() {
        inventoryTick++;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !PlayerContainer.mayRearrange(player)
                || SlotSwapper.instance().awaiting()) {
            return;
        }
        stockPlacement(player);
        stockTool(player);
    }

    private void stockPlacement(LocalPlayer player) {
        PlayerInventory inventory = new PlayerInventory(player);
        if (ThrowawayChooser.chosen(inventory) >= 0) {
            return;
        }
        int packed = HotbarFetcher.packed(player, ThrowawayChooser::isPreferred);
        if (packed >= 0) {
            SlotSwapper.instance().move(packed, InventorySlots.PLACEMENT);
        }
    }

    private void stockTool(LocalPlayer player) {
        var mining = Settings.holder().mining();
        if (!mining.autoTool()) {
            return;
        }
        if (inventoryTick % TOOL_CHECK_TICKS != 0) {
            return;
        }
        PlayerInventory inventory = new PlayerInventory(player);
        int best = BestSlot.bestAgainst(Blocks.STONE, mining.preferSilkTouch(), inventory, mining);
        if (InventorySlots.packed(best)) {
            SlotSwapper.instance().move(best, InventorySlots.TOOL);
        }
    }
}