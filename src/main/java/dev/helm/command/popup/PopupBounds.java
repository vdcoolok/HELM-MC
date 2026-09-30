package dev.helm.command.popup;

import net.minecraft.client.Minecraft;

public final class PopupBounds {

    private PopupBounds() {
    }

    public static int width() {
        return Minecraft.getInstance().getWindow().getGuiScaledWidth();
    }

    public static int height() {
        return Minecraft.getInstance().getWindow().getGuiScaledHeight();
    }
}