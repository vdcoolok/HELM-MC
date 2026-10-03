package dev.helm.mine;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.block.Block;

import dev.helm.command.ArgumentDefinition;
import dev.helm.command.ArgumentType;
import dev.helm.command.Command;
import dev.helm.command.CommandCall;
import dev.helm.command.CommandException;
import dev.helm.command.CommandFeedback;
import dev.helm.command.CommandResult;
import dev.helm.diag.Trace;
import dev.helm.mine.target.DeepslateVariants;
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
    private static final int NOT_A_COUNT = -1;
    private static final String LAST = "last";

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
                        "blocks to mine, then an optional count")
                        .offering(MineCommand::suggest));
    }

    private static CommandResult run(CommandCall call) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            ClientNotice.warn("Not in a world yet.");
            return CommandResult.FAILURE;
        }
        String raw = call.arguments().requireString(BLOCKS);
        Requested requested = request(raw);
        TargetFilter filter = Allowed.of(requested.filter(), Settings.holder().mining(),
                Settings.holder().movement());
        if (filter == null) {
            ClientNotice.warn("Breaking is turned off, so " + requested.filter().describe()
                    + " cannot be mined. List it in $set mining.breakAllowedAnyway first.");
            return CommandResult.FAILURE;
        }
        rememberNearbyChunks(filter);
        MineTask.instance().start(filter, requested.wanted(), raw);
        ClientNotice.warn("Mining " + filter.describe() + ".");
        return CommandResult.SUCCESS;
    }

    private static Requested request(String written) {
        List<String> tokens = dev.helm.command.chat.LineTokenizer.tokenize(written);
        int wanted = trailingCount(tokens);
        boolean expand = Settings.holder().mining().includeDeepslateVariants();
        List<TargetSelector> selectors = new ArrayList<>();
        for (String token : tokens) {
            if (count(token) != NOT_A_COUNT) {
                continue;
            }
            TargetSelector chosen = select(token);
            selectors.add(chosen);
            if (expand) {
                Block twin = DeepslateVariants.twinOf(chosen.block());
                if (twin != null && !selectors.contains(TargetSelector.whole(twin))) {
                    selectors.add(TargetSelector.whole(twin));
                }
            }
        }
        if (selectors.isEmpty()) {
            throw new CommandException(CommandFeedback.missingArgument(BLOCKS));
        }
        return new Requested(wanted, TargetFilter.of(selectors));
    }

    private static int trailingCount(List<String> tokens) {
        if (tokens.isEmpty()) {
            return 0;
        }
        String last = tokens.get(tokens.size() - 1);
        if (count(last) == NOT_A_COUNT) {
            return 0;
        }
        if (last.equals(LAST)) {
            throw new CommandException(CommandFeedback.invalidArgument(COUNT, last));
        }
        return count(last);
    }

    private static int count(String token) {
        if (token.isEmpty()) {
            return NOT_A_COUNT;
        }
        for (int index = 0; index < token.length(); index++) {
            if (!Character.isDigit(token.charAt(index))) {
                return NOT_A_COUNT;
            }
        }
        try {
            return Integer.parseInt(token);
        } catch (NumberFormatException tooLarge) {
            return NOT_A_COUNT;
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
