package dev.helm.diag;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

import dev.helm.command.Command;
import dev.helm.command.CommandResult;

public final class CommandTrace {

    private CommandTrace() {
    }

    public static void received(String rawLine) {
        Trace.instance().event("command", "typed " + describe(rawLine) + whereAmI());
    }

    public static void matched(Command command, List<String> arguments) {
        if (command == null) {
            Trace.instance().event("command", "matched nothing, tokens " + arguments);
            return;
        }
        Trace.instance().event("command", "resolved to `" + command.path() + "`"
                + (arguments.isEmpty() ? "" : " with " + arguments)
                + (command.isGroup() ? " (group, help only)" : ""));
    }

    public static void recorded(CommandResult result) {
        Trace.instance().event("command", "returned " + result);
    }

    public static void sent(String message) {
        Trace.instance().event("chat", "sent " + describe(message) + whereAmI());
    }

    private static String describe(String line) {
        String text = line == null ? "" : line.replace("\n", " ").trim();
        return text.isEmpty() ? "(empty)" : "`" + text + "`";
    }

    private static String whereAmI() {
        LocalPlayer player = Minecraft.getInstance().player;
        return player == null ? "" : " while at " + PlayerReport.everything(player);
    }
}
