package dev.helm.navigate;

import dev.helm.command.Command;
import dev.helm.command.CommandResult;
import dev.helm.diag.RouteTrace;
import dev.helm.diag.Trace;
import dev.helm.farm.FarmTask;
import dev.helm.pathfinding.goal.BlockGoal;
import dev.helm.pathfinding.search.SearchJob;
import dev.helm.setting.ClientNotice;

public final class GoToCommand {

    private GoToCommand() {
    }

    public static Command build() {
        return Command.leaf("goto", GoToCommand::run)
                .also("g", "go", "to")
                .describedAs("Walks to a block position.")
                .taking(
                        dev.helm.command.ArgumentDefinition.required("x",
                                dev.helm.command.ArgumentType.INTEGER, "block x"),
                        dev.helm.command.ArgumentDefinition.required("y",
                                dev.helm.command.ArgumentType.INTEGER, "block y"),
                        dev.helm.command.ArgumentDefinition.required("z",
                                dev.helm.command.ArgumentType.INTEGER, "block z"));
    }

    private static CommandResult run(dev.helm.command.CommandCall call) {
        int x = call.arguments().requireInteger("x");
        int y = call.arguments().requireInteger("y");
        int z = call.arguments().requireInteger("z");

        NavigatorAgent agent = NavigatorAgent.instance();
        if (!agent.navigator().ready()) {
            ClientNotice.warn("Not in a world yet.");
            return CommandResult.FAILURE;
        }
        var player = net.minecraft.client.Minecraft.getInstance().player;
        if (player == null) {
            ClientNotice.warn("Not in a world yet.");
            return CommandResult.FAILURE;
        }

        var position = player.blockPosition();
        Trace.instance().barrier("goto");
        FarmTask.instance().stop();
        Trace.instance().event("goto", "requested " + x + " " + y + " " + z
                + " from " + position.getX() + " " + position.getY() + " " + position.getZ()
                + " facing " + String.format("%.1f/%.1f", player.getYRot(), player.getXRot()));
        Trace.instance().event("goto", "settings: break=" + agent.movement().allowBreak()
                + " place=" + agent.movement().allowPlace()
                + " parkour=" + agent.movement().parkourAllowed()
                + " sprint=" + agent.movement().sprintAllowed()
                + " maxFall=" + agent.movement().maxFallHeightNoWater()
                + " autotool=" + dev.helm.setting.Settings.holder().mining().autoTool());

        if (position.getX() == x && position.getY() == y && position.getZ() == z) {
            Trace.instance().event("goto", "already standing on the goal");
            agent.pilot().halt();
            agent.pilot().forgetObjective();
            ClientNotice.warn("Already at " + x + " " + y + " " + z + ".");
            return CommandResult.SUCCESS;
        }

        agent.pilot().forgetObjective();
        SearchJob job = agent.navigator().searchFor(new BlockGoal(x, y, z),
                position.getX(), position.getY(), position.getZ());
        if (job == null) {
            ClientNotice.warn("Not in a world yet.");
            return CommandResult.FAILURE;
        }
        agent.pilot().await(job, Objective.at(x, y, z));
        ClientNotice.warn("Searching for a way to " + x + " " + y + " " + z + ".");
        return CommandResult.SUCCESS;
    }
}