package dev.helm.follow;

import dev.helm.command.ArgumentDefinition;
import dev.helm.command.Command;
import dev.helm.command.CommandCall;
import dev.helm.command.CommandResult;
import dev.helm.follow.subject.FollowSubject;
import dev.helm.follow.subject.SubjectReader;
import dev.helm.navigate.NavigatorAgent;
import dev.helm.setting.ClientNotice;
import net.minecraft.client.Minecraft;

public final class FollowCommand {

    private static final String SUBJECT = "subject";

    private FollowCommand() {
    }

    public static Command build() {
        return Command.leaf("follow", FollowCommand::run)
                .also("chase", "stalk")
                .describedAs("Walks after mobs and players.")
                .taking(ArgumentDefinition.rest(SUBJECT, "mob and player names"));
    }

    private static CommandResult run(CommandCall call) {
        var client = Minecraft.getInstance();
        if (client.player == null || !NavigatorAgent.instance().navigator().ready()) {
            ClientNotice.warn("Not in a world yet.");
            return CommandResult.FAILURE;
        }
        FollowSubject subject = SubjectReader.read(call.arguments().requireString(SUBJECT),
                client.level);
        FollowTask.instance().start(subject);
        ClientNotice.warn(subject.announce());
        return CommandResult.SUCCESS;
    }
}