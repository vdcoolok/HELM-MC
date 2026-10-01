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
            boolean searching = agent.pilot().searching();
            boolean walking = agent.pilot().isWalking();
            agent.pilot().halt();
            agent.pilot().forgetDestination();
            agent.navigator().cancelSearch();
            if (searching) {
                ClientNotice.warn("Stopped searching.");
                return CommandResult.SUCCESS;
            }
            if (!walking) {
                ClientNotice.warn("Nothing to stop.");
                return CommandResult.FAILURE;
            }
            ClientNotice.warn("Stopped.");
            return CommandResult.SUCCESS;
        }).also("cancel", "abort", "halt").describedAs("Stops walking and releases all controls.");
    }
}