package dev.helm.command.chat;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import dev.helm.command.CommandOutput;

public final class SystemMessageOutput implements CommandOutput {

    private final Minecraft client;

    public SystemMessageOutput(Minecraft client) {
        this.client = client;
    }

    @Override
    public void feedback(Component message) {
        client.gui.hud.getChat().addClientSystemMessage(message);
    }

    @Override
    public void error(Component message) {
        client.gui.hud.getChat().addClientSystemMessage(message);
    }
}
