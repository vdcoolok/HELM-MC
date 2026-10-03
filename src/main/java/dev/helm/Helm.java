package dev.helm;

import dev.helm.aim.LookController;
import dev.helm.aim.MouseScale;
import dev.helm.command.CommandSystem;
import dev.helm.diag.Trace;
import dev.helm.diag.WorldReport;
import dev.helm.drops.DropCache;
import dev.helm.macro.runtime.MacroController;
import dev.helm.navigate.NavigatorAgent;
import dev.helm.outline.Silhouettes;
import dev.helm.setting.Settings;
import dev.helm.setting.SettingsFile;
import dev.helm.storage.HelmStorage;
import dev.helm.world.cache.WorldCache;
import dev.helm.tools.BlockAvoidList;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;

public final class Helm implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        Trace.instance().start();
        Trace.instance().event("startup", "HELM initialising");
        Trace.instance().event("startup", "java " + System.getProperty("java.version")
                + " on " + System.getProperty("os.name") + " " + System.getProperty("os.arch"));

        Settings.holder().restoreDefaults();
        SettingsFile.load(HelmStorage.root().resolve(SettingsFile.fileName()));
        BlockAvoidList.refresh(Settings.holder().mining());
        Trace.instance().event("startup", "settings loaded, "
                + Settings.holder().movement().declared().size()
                + " movement, " + Settings.holder().mining().declared().size()
                + " mining, " + Settings.holder().look().declared().size()
                + " look, " + Settings.holder().path().declared().size() + " path");

        var client = Minecraft.getInstance();
        if (client.options != null) {
            double sensitivity = client.options.sensitivity().get();
            LookController.instance().updateSettings(
                    MouseScale.fromOptions(sensitivity), Settings.holder().look());
            Trace.instance().event("startup", "mouse sensitivity " + sensitivity);
        } else {
            Trace.instance().event("startup", "options not ready, aim scale deferred");
        }

        CommandSystem.start();
        MacroController.instance().install();
        Silhouettes.install();
        Trace.instance().event("startup", "commands, macros and outlines installed");

        ClientPlayConnectionEvents.JOIN.register((handler, sender, game) -> {
            Trace.instance().barrier("join");
            Trace.instance().event("join", "world joined: " + game.level.dimension().identifier());
            WorldReport.restart();
            HelmStorage.prepare();
            WorldCache.open(game.level);
            NavigatorAgent.instance().onJoin();
            reportWorld("join");
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, game) -> {
            Trace.instance().event("join", "world left");
            WorldCache.close();
            DropCache.instance().clear();
            NavigatorAgent.instance().onDisconnect();
        });
    }

    private static void reportWorld(String area) {
        var client = Minecraft.getInstance();
        if (client.level == null || client.player == null) {
            Trace.instance().event(area, "world or player still null");
            return;
        }
        var agent = NavigatorAgent.instance();
        int lowest = client.level.getMinY();
        Trace.instance().event(area, "navigator ready=" + agent.navigator().ready()
                + " build height " + lowest + " to " + (lowest + client.level.getHeight())
                + " mode " + client.gameMode.getPlayerMode());
    }
}
