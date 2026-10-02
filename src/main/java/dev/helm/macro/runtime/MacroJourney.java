package dev.helm.macro.runtime;

import net.minecraft.client.Minecraft;

import dev.helm.macro.MacroStatement;
import dev.helm.navigate.Objective;
import dev.helm.navigate.NavigatorAgent;

final class MacroJourney implements MacroRunner.Ongoing {

    private final NavigatorAgent agent;
    private final Objective target;
    private boolean started;

    MacroJourney(NavigatorAgent agent, Objective target) {
        this.agent = agent;
        this.target = target;
    }

    static MacroJourney towards(NavigatorAgent agent, MacroStatement.Move move) {
        int x = (int) Math.floor(move.x());
        int y = (int) Math.floor(move.y());
        int z = (int) Math.floor(move.z());
        return new MacroJourney(agent, Objective.at(x, y, z));
    }

    static MacroJourney holding(NavigatorAgent agent, Objective target) {
        return new MacroJourney(agent, target);
    }

    @Override
    public boolean done() {
        var player = Minecraft.getInstance().player;
        if (player == null) {
            throw MacroFailure.noWorld();
        }
        var feet = player.blockPosition();
        if (target.satisfiedBy(feet.getX(), feet.getY(), feet.getZ())) {
            return true;
        }
        var pilot = agent.pilot();
        if (pilot.searching() || pilot.isWalking()) {
            return false;
        }
        if (started) {
            throw MacroFailure.unreachable(target.label());
        }
        start();
        return false;
    }

    private void start() {
        if (!agent.navigator().ready()) {
            throw MacroFailure.noWorld();
        }
        var feet = Minecraft.getInstance().player.blockPosition();
        var job = agent.navigator().searchFor(target.goal(),
                feet.getX(), feet.getY(), feet.getZ());
        if (job == null) {
            throw MacroFailure.noWorld();
        }
        started = true;
        agent.pilot().await(job, target);
    }
}
