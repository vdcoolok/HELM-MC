package dev.helm;

import dev.helm.client.HelmClient;
import net.fabricmc.api.ClientModInitializer;

public final class Helm implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        HelmClient.start();
    }
}
