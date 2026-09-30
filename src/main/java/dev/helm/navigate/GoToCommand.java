package dev.helm.navigate;

import dev.helm.command.Command;
import dev.helm.command.CommandResult;
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
        var search = agent.navigator().searchTo(new dev.helm.pathfinding.goal.BlockGoal(x, y, z),
                position.getX(), position.getY(), position.getZ());
        search.run(System.currentTimeMillis(), System::nanoTime);
        Journey.Result result = Journey.collect(search, agent.navigator().blocks(),
                agent.navigator().walk());

        if (!result.usable()) {
            ClientNotice.warn("No path to " + x + " " + y + " " + z + ".");
            return CommandResult.FAILURE;
        }
        agent.pilot().travel(result.route());
        ClientNotice.warn((result.reached() ? "Path found: " : "Partial path: ")
                + result.route().length() + " steps.");
        return CommandResult.SUCCESS;
    }
}
