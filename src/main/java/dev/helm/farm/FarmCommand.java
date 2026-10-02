package dev.helm.farm;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;

import dev.helm.command.ArgumentDefinition;
import dev.helm.command.ArgumentType;
import dev.helm.command.Command;
import dev.helm.command.CommandCall;
import dev.helm.command.CommandException;
import dev.helm.command.CommandFeedback;
import dev.helm.command.CommandResult;
import dev.helm.setting.ClientNotice;

public final class FarmCommand {

    private FarmCommand() {
    }

    public static Command build() {
        return Command.leaf("farm", FarmCommand::run)
                .also("farming", "harvest")
                .describedAs("Harvests ripe crops nearby and plants the empty ones again.")
                .taking(ArgumentDefinition.optional("range", ArgumentType.INTEGER,
                        "blocks around you to work on, 0 for everywhere"));
    }

    private static CommandResult run(CommandCall call) {
        if (!call.arguments().has("range")) {
            return start(FarmArea.everywhere(whereStanding()));
        }
        int range = call.arguments().requireInteger("range");
        if (range < 0) {
            throw new CommandException(CommandFeedback.invalidArgument("range",
                    String.valueOf(range)));
        }
        return start(new FarmArea(whereStanding(), range));
    }

    private static CommandResult start(FarmArea area) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            ClientNotice.warn("Not in a world yet.");
            return CommandResult.FAILURE;
        }
        FarmTask.instance().start(area);
        ClientNotice.warn("Farming " + area.describe() + ".");
        return CommandResult.SUCCESS;
    }

    private static BlockPos whereStanding() {
        return Minecraft.getInstance().player.blockPosition();
    }
}