package dev.helm;

import dev.helm.aim.LookController;
import dev.helm.aim.MouseScale;
import dev.helm.command.CommandSystem;
import dev.helm.diag.Trace;
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
        Trace.instance().event("startup", "commands and macros installed");

        ClientPlayConnectionEvents.JOIN.register((handler, sender, game) -> {
            Trace.instance().barrier("join");
            Trace.instance().event("join", "world joined: " + game.level.dimension().identifier());
            HelmStorage.prepare();
            NavigatorAgent.instance().onJoin();
            reportWorld("post-join");
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, game) -> {
            Trace.instance().event("join", "world left");
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
        Trace.instance().event(area, "navigator ready=" + agent.navigator().ready()
                + " y " + client.level.getMinY() + ".." + client.level.getMinY()
                + client.level.getHeight());
        Trace.instance().event(area, "player at " + describe(client.player.blockPosition())
                + " yaw " + round(client.player.getYRot())
                + " pitch " + round(client.player.getXRot())
                + " onGround " + client.player.onGround()
                + " mode " + client.gameMode.getPlayerMode());
        Trace.instance().event(area, "looking at "
                + (client.hitResult == null ? "nothing"
                        : String.valueOf(client.hitResult.getType())));
    }

    private static String describe(net.minecraft.core.BlockPos pos) {
        return pos.getX() + " " + pos.getY() + " " + pos.getZ();
    }

    private static String round(double value) {
        return String.format("%.2f", value);
    }
}
