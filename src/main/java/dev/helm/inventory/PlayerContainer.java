package dev.helm.inventory;

import net.minecraft.client.player.LocalPlayer;

import dev.helm.setting.Settings;

public final class PlayerContainer {

    private PlayerContainer() {
    }

    public static boolean isOwnInventory(LocalPlayer player) {
        return player.containerMenu == player.inventoryMenu;
    }

    public static boolean mayRearrange(LocalPlayer player) {
        return Settings.holder().movement().allowInventory() && isOwnInventory(player);
    }
}