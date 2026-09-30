package dev.helm.navigate;

import dev.helm.command.Command;
import dev.helm.command.CommandResult;
import dev.helm.setting.ClientNotice;

public final class StopCommand {

    private StopCommand() {
    }

    public static Command build() {
        return Command.leaf("stop", call -> {
            NavigatorAgent agent = NavigatorAgent.instance();
            if (!agent.pilot().isWalking()) {
                ClientNotice.warn("Nothing to stop.");
                return CommandResult.FAILURE;
            }
            agent.pilot().halt();
            ClientNotice.warn("Stopped.");
            return CommandResult.SUCCESS;
        }).also("cancel", "abort", "halt").describedAs("Stops walking and releases all controls.");
    }
}
