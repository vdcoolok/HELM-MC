package dev.helm.mine;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;

import dev.helm.command.ArgumentDefinition;
import dev.helm.command.ArgumentType;
import dev.helm.command.Command;
import dev.helm.command.CommandCall;
import dev.helm.command.CommandException;
import dev.helm.command.CommandFeedback;
import dev.helm.command.CommandResult;
import dev.helm.diag.Trace;
import dev.helm.mine.target.TargetCompletions;
import dev.helm.mine.target.TargetFilter;
import dev.helm.mine.target.TargetSelector;
import dev.helm.mine.target.TargetSelectorReader;
import dev.helm.setting.ClientNotice;
import dev.helm.setting.Settings;
import dev.helm.world.cache.WorldCache;

public final class MineCommand {

    private static final String BLOCKS = "blocks";
    private static final String COUNT = "count";

    private record Requested(int wanted, TargetFilter filter) {
    }

    private MineCommand() {
    }

    public static Command build() {
        return Command.leaf("mine", MineCommand::run)
                .also("dig", "excavate")
                .describedAs("Searches for and mines blocks until they run out.")
                .pickingBlocks()
                .taking(ArgumentDefinition.required(BLOCKS, ArgumentType.STRING,
                        "optional count, then the blocks to mine")
                        .offering(MineCommand::suggest));
    }

    private static CommandResult run(CommandCall call) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            ClientNotice.warn("Not in a world yet.");
            return CommandResult.FAILURE;
        }
        Requested requested = request(call.arguments().requireString(BLOCKS));
        TargetFilter filter = Allowed.of(requested.filter(), Settings.holder().mining(),
                Settings.holder().movement());
        if (filter == null) {
            ClientNotice.warn("Breaking is turned off, so " + requested.filter().describe()
                    + " cannot be mined. List it in $set mining.breakAllowedAnyway first.");
            return CommandResult.FAILURE;
        }
        rememberNearbyChunks(filter);
        MineTask.instance().start(filter, requested.wanted());
        ClientNotice.warn("Mining " + filter.describe() + ".");
        return CommandResult.SUCCESS;
    }

    private static Requested request(String written) {
        List<String> tokens = dev.helm.command.chat.LineTokenizer.tokenize(written);
        int first = leadingCount(tokens);
        List<TargetSelector> selectors = new ArrayList<>();
        for (int index = first; index < tokens.size(); index++) {
            selectors.add(select(tokens.get(index)));
        }
        if (selectors.isEmpty()) {
            throw new CommandException(CommandFeedback.missingArgument(BLOCKS));
        }
        return new Requested(first, TargetFilter.of(selectors));
    }

    private static int leadingCount(List<String> tokens) {
        if (tokens.isEmpty()) {
            return 0;
        }
        String lead = tokens.get(0);
        int wanted = count(lead);
        if (wanted < 0) {
            throw new CommandException(CommandFeedback.invalidArgument(COUNT, lead));
        }
        return wanted;
    }

    private static int count(String token) {
        try {
            return Integer.parseInt(token);
        } catch (NumberFormatException notACount) {
            return 0;
        }
    }

    private static TargetSelector select(String token) {
        try {
            return TargetSelectorReader.read(token);
        } catch (IllegalArgumentException malformed) {
            throw new CommandException(malformed.getMessage());
        }
    }

    private static void rememberNearbyChunks(TargetFilter filter) {
        Minecraft client = Minecraft.getInstance();
        WorldCache cache = WorldCache.get();
        if (client.level == null || client.player == null || cache == null) {
            return;
        }
        int radius = Settings.holder().mining().repackRadius();
        int remembered = ChunkRemembering.around(cache, client.level, client.player, radius);
        Trace.instance().event("mine", "queued " + remembered + " chunks within " + radius
                + " chunks to be remembered before looking for " + filter.describe());
    }

    private static List<String> suggest() {
        return TargetCompletions.identifiers();
    }
}