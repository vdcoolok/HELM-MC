package dev.helm.navigate;

import dev.helm.aim.LookController;
import dev.helm.command.Command;
import dev.helm.command.CommandResult;
import dev.helm.farm.FarmTask;
import dev.helm.follow.FollowTask;
import dev.helm.macro.runtime.MacroController;
import dev.helm.mine.MineTask;
import dev.helm.setting.ClientNotice;

public final class StopCommand {

    private StopCommand() {
    }

    public static Command build() {
        return Command.leaf("stop", call -> {
            NavigatorAgent agent = NavigatorAgent.instance();
            boolean searching = agent.pilot().searching();
            boolean walking = agent.pilot().isWalking();
            boolean anchored = agent.pilot().anchored();
            boolean holding = LookController.instance().holding();
            boolean macro = MacroController.instance().active().isPresent();
            boolean farm = FarmTask.instance().running();
            boolean mining = MineTask.instance().running();
            boolean following = FollowTask.instance().running();

            agent.pilot().halt();
            agent.pilot().forgetObjective();
            agent.navigator().cancelSearch();
            LookController.instance().release();
            if (macro) {
                MacroController.instance().halt();
            }
            if (farm) {
                FarmTask.instance().stop();
            }
            if (mining) {
                MineTask.instance().stop();
            }
            if (following) {
                FollowTask.instance().stop();
            }

            StringBuilder what = new StringBuilder();
            append(what, searching || walking || anchored, "Walking stopped");
            append(what, holding, "Look released");
            append(what, macro, "Macro stopped");
            append(what, farm, "Farming stopped");
            append(what, mining, "Mining stopped");
            append(what, following, "Following stopped");
            if (what.isEmpty()) {
                ClientNotice.warn("Nothing to stop.");
                return CommandResult.FAILURE;
            }
            ClientNotice.warn(what + ".");
            return CommandResult.SUCCESS;
        }).also("cancel", "abort", "halt")
                .describedAs("Stops walking, mining, farming, macros and any held position "
                        + "or facing.");
    }

    private static void append(StringBuilder what, boolean condition, String text) {
        if (!condition) {
            return;
        }
        if (!what.isEmpty()) {
            what.append(". ");
        }
        what.append(text);
    }
}