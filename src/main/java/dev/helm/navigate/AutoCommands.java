package dev.helm.navigate;

import dev.helm.aim.Aim;
import dev.helm.aim.LookController;
import dev.helm.command.ArgumentDefinition;
import dev.helm.command.ArgumentType;
import dev.helm.command.Command;
import dev.helm.command.CommandCall;
import dev.helm.command.CommandException;
import dev.helm.command.CommandResult;
import dev.helm.diag.Trace;
import dev.helm.farm.FarmTask;
import dev.helm.pathfinding.goal.BlockGoal;
import dev.helm.pathfinding.search.SearchJob;
import dev.helm.setting.ClientNotice;
import net.minecraft.client.Minecraft;

public final class AutoCommands {

    private AutoCommands() {
    }

    public static Command[] all() {
        return new Command[] {autoGoto(), autoGoToHere(), autoLookAt(), autoLookAtHere()};
    }

    public static Command autoGoto() {
        return Command.leaf("autogoto", call -> walk(call,
                        call.arguments().requireInteger("x"),
                        call.arguments().requireInteger("y"),
                        call.arguments().requireInteger("z")))
                .also("agoto")
                .describedAs("Walks to a block position and keeps going back if moved off it.")
                .taking(
                        ArgumentDefinition.required("x",
                                ArgumentType.INTEGER, "block x"),
                        ArgumentDefinition.required("y",
                                ArgumentType.INTEGER, "block y"),
                        ArgumentDefinition.required("z",
                                ArgumentType.INTEGER, "block z"));
    }

    public static Command autoGoToHere() {
        return Command.leaf("autogotohere", call -> {
            var feet = NavigatorAgent.instance().pilot().feet();
            if (feet == null) {
                ClientNotice.warn("Not in a world yet.");
                return CommandResult.FAILURE;
            }
            return walk(call, feet[0], feet[1], feet[2]);
        }).also("agotohere").describedAs("Walks back to where you are and holds that spot.");
    }

    public static Command autoLookAt() {
        return Command.leaf("autolookat", call -> {
            String angles = call.arguments().requireString("angles");
            var colon = angles.indexOf('/');
            if (colon < 0) {
                throw new CommandException("Expected 'pitch / yaw', e.g. 14/240");
            }
            return hold(parse(angles.substring(0, colon), "pitch"),
                    parse(angles.substring(colon + 1), "yaw"), angles.trim());
        }).also("alookat")
                .describedAs("Locks where you are facing, overriding your mouse until stopped.")
                .taking(ArgumentDefinition.required("angles",
                        ArgumentType.STRING, "pitch / yaw"));
    }

    public static Command autoLookAtHere() {
        return Command.leaf("autolookathere", call -> {
            var player = Minecraft.getInstance().player;
            if (player == null) {
                ClientNotice.warn("Not in a world yet.");
                return CommandResult.FAILURE;
            }
            Aim here = new Aim(player.getYRot(), player.getXRot());
            return hold(here.yaw(), here.pitch(), String.format("%.1f/%.1f", here.pitch(), here.yaw()));
        }).also("alookathere").describedAs("Locks where you are facing right now.");
    }

    private static CommandResult walk(CommandCall call, int x, int y, int z) {
        NavigatorAgent agent = NavigatorAgent.instance();
        if (!agent.navigator().ready()) {
            ClientNotice.warn("Not in a world yet.");
            return CommandResult.FAILURE;
        }
        var feet = agent.pilot().feet();
        if (feet == null) {
            ClientNotice.warn("Not in a world yet.");
            return CommandResult.FAILURE;
        }

        Trace.instance().barrier("autogoto");
        Trace.instance().event("goto", "anchoring on " + x + " " + y + " " + z
                + " from " + feet[0] + " " + feet[1] + " " + feet[2]);
        FarmTask.instance().stop();
        dev.helm.mine.MineTask.instance().stop();
        dev.helm.follow.FollowTask.instance().stop();

        Objective target = Objective.at(x, y, z);
        agent.pilot().anchorAt(target);
        if (feet[0] == x && feet[1] == y && feet[2] == z) {
            Trace.instance().event("goto", "already standing on the anchored spot");
            agent.pilot().halt();
            ClientNotice.warn("Holding " + x + " " + y + " " + z + ".");
            return CommandResult.SUCCESS;
        }

        SearchJob job = agent.navigator().searchFor(new BlockGoal(x, y, z),
                feet[0], feet[1], feet[2]);
        if (job == null) {
            ClientNotice.warn("Not in a world yet.");
            return CommandResult.FAILURE;
        }
        agent.pilot().await(job, target);
        ClientNotice.warn("Holding " + x + " " + y + " " + z + ".");
        return CommandResult.SUCCESS;
    }

    private static CommandResult hold(double pitch, double yaw, String shown) {
        FarmTask.instance().stop();
        dev.helm.mine.MineTask.instance().stop();
        dev.helm.follow.FollowTask.instance().stop();
        LookController.instance().hold(new Aim(yaw, pitch));
        Trace.instance().barrier("autolookat");
        Trace.instance().event("look", "holding " + shown
                + ", the mouse will not move the camera until $stop");
        ClientNotice.warn("Holding " + shown + ".");
        return CommandResult.SUCCESS;
    }

    private static double parse(String token, String axis) {
        try {
            return Double.parseDouble(token.trim());
        } catch (NumberFormatException invalid) {
            throw new CommandException("Invalid " + axis + ": " + token.trim());
        }
    }
}