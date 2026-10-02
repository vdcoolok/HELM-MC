package dev.helm.inventory;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.InventoryMenu;

import dev.helm.diag.Trace;
import dev.helm.setting.MovementSettings;
import dev.helm.setting.Settings;
import dev.helm.world.PlayerInventory;

public final class SlotSwapper {

    private static final SlotSwapper INSTANCE = new SlotSwapper();

    private int ticksSinceMove;
    private Move awaiting;

    public static SlotSwapper instance() {
        return INSTANCE;
    }

    public void onTick() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !PlayerContainer.mayRearrange(player)) {
            this.awaiting = null;
            return;
        }
        MovementSettings movement = Settings.holder().movement();
        ticksSinceMove++;
        Move wanted = awaiting;
        if (wanted != null) {
            send(player, movement, wanted);
        }
    }

    public boolean move(int from, int to) {
        if (!InventorySlots.carried(from) || !InventorySlots.onHotbar(to)) {
            return false;
        }
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !PlayerContainer.mayRearrange(player)) {
            return false;
        }
        MovementSettings movement = Settings.holder().movement();
        Move wanted = new Move(from, to);
        if (!send(player, movement, wanted)) {
            return false;
        }
        new PlayerInventory(player).selectSlot(to);
        return true;
    }

    public boolean awaiting() {
        return awaiting != null;
    }

    private boolean send(LocalPlayer player, MovementSettings movement, Move wanted) {
        this.awaiting = wanted;
        int gap = movement.ticksBetweenInventoryMoves();
        if (ticksSinceMove < gap) {
            Trace.instance().pulse("inventory-gap", "inventory", "holding a swap of slot "
                    + wanted.from() + " with slot " + wanted.to() + " for "
                    + (gap - ticksSinceMove) + " more ticks");
            return false;
        }
        if (movement.inventoryMoveOnlyIfStationary() && !stationary(player)) {
            Trace.instance().pulse("inventory-still", "inventory",
                    "holding a swap of slot " + wanted.from() + " with slot " + wanted.to()
                            + " until the player stops");
            return false;
        }
        var mode = Minecraft.getInstance().gameMode;
        if (mode == null) {
            return false;
        }
        mode.handleContainerInput(InventoryMenu.CONTAINER_ID, wanted.from(), wanted.to(),
                ContainerInput.SWAP, player);
        ticksSinceMove = 0;
        this.awaiting = null;
        Trace.instance().event("inventory", "swapped slot " + wanted.from()
                + " with hotbar slot " + wanted.to());
        return true;
    }

    private static boolean stationary(LocalPlayer player) {
        return player.getDeltaMovement().multiply(1.0D, 0.0D, 1.0D).length() < 0.00001D;
    }

    private record Move(int from, int to) {
    }
}