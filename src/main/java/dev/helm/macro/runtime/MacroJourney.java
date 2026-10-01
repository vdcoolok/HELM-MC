package dev.helm.macro.runtime;

import net.minecraft.client.Minecraft;

import dev.helm.macro.MacroStatement;
import dev.helm.navigate.Destination;
import dev.helm.navigate.NavigatorAgent;
import dev.helm.navigate.SegmentPlanner;

final class MacroJourney implements MacroRunner.Ongoing {

    private final NavigatorAgent agent;
    private final Destination target;
    private boolean started;

    MacroJourney(NavigatorAgent agent, Destination target) {
        this.agent = agent;
        this.target = target;
    }

    static MacroJourney towards(NavigatorAgent agent, MacroStatement.Move move) {
        int x = (int) Math.floor(move.x());
        int y = (int) Math.floor(move.y());
        int z = (int) Math.floor(move.z());
        return new MacroJourney(agent, new Destination(x, y, z));
    }

    @Override
    public boolean done() {
        var player = Minecraft.getInstance().player;
        if (player == null) {
            throw MacroFailure.noWorld();
        }
        if (target.reachedBy(player.blockPosition())) {
            return true;
        }
        var pilot = agent.pilot();
        if (pilot.searching() || pilot.isWalking()) {
            return false;
        }
        if (started) {
            throw MacroFailure.unreachable(target.x(), target.y(), target.z());
        }
        start();
        return false;
    }

    private void start() {
        if (!agent.navigator().ready()) {
            throw MacroFailure.noWorld();
        }
        var feet = Minecraft.getInstance().player.blockPosition();
        var job = SegmentPlanner.first(agent.navigator(), target.goal(),
                feet.getX(), feet.getY(), feet.getZ());
        if (job == null) {
            throw MacroFailure.noWorld();
        }
        started = true;
        agent.pilot().await(job, target);
    }
}
