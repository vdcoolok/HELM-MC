package dev.helm.setting;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public final class ClientNotice {

    private ClientNotice() {
    }

    public static void warn(String message) {
        send(Component.literal(message));
    }

    public static void send(Component message) {
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.gui == null || client.gui.hud == null) {
            return;
        }
        client.gui.hud.getChat().addClientSystemMessage(message);
    }
}
