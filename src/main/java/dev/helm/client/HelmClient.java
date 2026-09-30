package dev.helm.client;

import dev.helm.command.HelmCommands;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;

public final class HelmClient {

    private HelmClient() {
    }

    public static void start() {
        ClientCommandRegistrationCallback.EVENT.register(HelmCommands::register);
    }

    public static Minecraft client() {
        return Minecraft.getInstance();
    }

    public static LocalPlayer player() {
        return client().player;
    }

    public static ClientLevel level() {
        return client().level;
    }

    public static boolean inWorld() {
        return player() != null && level() != null;
    }
}
