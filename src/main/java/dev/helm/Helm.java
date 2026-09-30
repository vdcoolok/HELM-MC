package dev.helm;

import dev.helm.command.CommandSystem;
import net.fabricmc.api.ClientModInitializer;

public final class Helm implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        CommandSystem.start();
    }
}
