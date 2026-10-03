package dev.helm.pathfinding.search;

import java.util.Set;
import java.util.function.UnaryOperator;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.block.Block;

import dev.helm.pathfinding.context.Tunables;
import dev.helm.pathfinding.context.TunablesFactory;
import dev.helm.pathfinding.goal.Goal;
import dev.helm.pathfinding.move.FrozenEnvironment;
import dev.helm.pathfinding.move.MoveEnvironment;
import dev.helm.pathfinding.move.MoveExpander;
import dev.helm.pathfinding.move.expand.Expanders;
import dev.helm.pathfinding.world.BlockView;
import dev.helm.world.ClientLevelView;
import dev.helm.pathfinding.world.block.WalkRules;
import dev.helm.world.PlayerInventory;
import dev.helm.world.read.FrozenChunks;
import dev.helm.world.read.WorldBounds;

public final class SearchCoordinator {

    private final SearchPool pool = new SearchPool();

    private SearchJob inProgress;

    public boolean searching() {
        return inProgress != null;
    }

    public SearchJob inProgress() {
        return inProgress;
    }

    public SearchJob submit(Goal goal, int fromX, int fromY, int fromZ,
                            Set<Block> doNotBreak) {
        return submit(goal, fromX, fromY, fromZ, doNotBreak, tunables -> tunables);
    }

    public SearchJob submit(Goal goal, int fromX, int fromY, int fromZ,
                            Set<Block> doNotBreak,
                            UnaryOperator<Tunables> adjustTunables) {
        cancel();

        Minecraft client = Minecraft.getInstance();
        ClientLevel level = client.level;
        LocalPlayer player = client.player;
        if (level == null || player == null) {
            return null;
        }

        BlockView frozen = frozenView(level, doNotBreak);
        Tunables tuning = TunablesFactory.fromSettings(adjustTunables);
        WalkRules walk = new WalkRules(frozen, tuning);
        var strength = new dev.helm.tools.MinedToolStrength(new PlayerInventory(player),
                dev.helm.setting.Settings.holder().mining(),
                dev.helm.setting.Settings.holder().movement());
        var work = new dev.helm.pathfinding.world.block.WorkCosts(frozen, walk, tuning, strength);
        MoveEnvironment environment = new FrozenEnvironment(frozen, walk, work, tuning);
        MoveExpander[] expanders = Expanders.forEnvironment(environment);

        var budget = new SearchBudget(
                dev.helm.setting.Settings.holder().path().primaryTimeoutMillis(),
                dev.helm.setting.Settings.holder().path().failureTimeoutMillis(),
                dev.helm.setting.Settings.holder().path().maxChunkBorderFetch(),
                dev.helm.setting.Settings.holder().path().repropagateImprovement()
                        ? SearchBudget.MIN_IMPROVEMENT : 0);

        Search search = new Search(fromX, fromY, fromZ,
                GoalTarget.reachable(goal, frozen, dev.helm.setting.Settings.holder().path()),
                environment, expanders, budget);
        SearchJob job = new SearchJob(search);
        inProgress = job;
        pool.submit(job::execute);
        return job;
    }

    public boolean cancel() {
        SearchJob running = inProgress;
        if (running == null) {
            return false;
        }
        running.search().cancel();
        inProgress = null;
        return true;
    }

    public SearchJob finished() {
        SearchJob running = inProgress;
        if (running == null || !running.done()) {
            return null;
        }
        inProgress = null;
        return running;
    }

    public void shutdown() {
        cancel();
        pool.shutdown();
    }

    private BlockView frozenView(ClientLevel level, Set<Block> doNotBreak) {
        var cache = dev.helm.setting.Settings.holder().cache();
        FrozenChunks chunks = FrozenChunks.capture(level.getChunkSource());
        return new ClientLevelView(level, chunks,
                new WorldBounds(level.getWorldBorder()), cache.preferLoaded(),
                cache.enabled(), doNotBreak);
    }
}