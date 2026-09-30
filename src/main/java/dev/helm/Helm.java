package dev.helm;

import dev.helm.aim.LookController;
import dev.helm.command.CommandSystem;
import dev.helm.macro.runtime.MacroController;
import dev.helm.navigate.NavigatorAgent;
import dev.helm.setting.Settings;
import dev.helm.setting.SettingsFile;
import dev.helm.storage.HelmStorage;
import dev.helm.tools.BlockAvoidList;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;

public final class Helm implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        Settings.holder().restoreDefaults();
        SettingsFile.load(HelmStorage.root().resolve(SettingsFile.fileName()));
        BlockAvoidList.refresh(Settings.holder().mining());

        var client = Minecraft.getInstance();
        if (client.options != null) {
            LookController.instance().updateSettings(
                    dev.helm.aim.MouseScale.fromOptions(client.options.sensitivity().get()),
                    Settings.holder().look());
        }

        CommandSystem.start();
        MacroController.instance().install();

        ClientPlayConnectionEvents.JOIN.register((handler, sender, game) -> {
            HelmStorage.prepare();
            NavigatorAgent.instance().onJoin();
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, game) -> {
            NavigatorAgent.instance().onDisconnect();
        });
    }
}
