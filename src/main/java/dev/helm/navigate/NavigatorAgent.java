package dev.helm.navigate;

import dev.helm.movement.RouteWalker;
import dev.helm.movement.StepPricer;
import dev.helm.movement.step.StepContext;
import dev.helm.setting.LookSettings;
import dev.helm.setting.MovementSettings;
import dev.helm.setting.Settings;
import dev.helm.world.cache.WorldCache;
import net.minecraft.client.Minecraft;

public final class NavigatorAgent {

    private static final NavigatorAgent INSTANCE = new NavigatorAgent();

    private final Navigator navigator = new Navigator();
    private final Pilot pilot;

    private NavigatorAgent() {
        this.pilot = new Pilot(new RouteWalker(
                new StepPricer(navigator.expanders()), Settings.holder().movement()));
    }

    public static NavigatorAgent instance() {
        return INSTANCE;
    }

    public Navigator navigator() {
        return navigator;
    }

    public Pilot pilot() {
        return pilot;
    }

    public void onJoin() {
        navigator.refresh();
        pilot.setContext(new StepContext(navigator.blocks(), navigator.walk(),
                Settings.holder().look(), Settings.holder().movement()));
    }

    public void onTick() {
        WorldCache cache = WorldCache.get();
        if (cache != null) {
            var client = Minecraft.getInstance();
            if (client.player != null) {
                cache.tick(client.player.getBlockX(), client.player.getBlockZ());
            }
        }
        pilot.tick();
    }

    public void onDisconnect() {
        pilot.halt();
        navigator.shutdown();
    }

    public LookSettings look() {
        return Settings.holder().look();
    }

    public MovementSettings movement() {
        return Settings.holder().movement();
    }
}
