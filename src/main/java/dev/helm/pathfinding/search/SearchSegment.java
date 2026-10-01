package dev.helm.pathfinding.search;

import java.util.Set;

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
import dev.helm.pathfinding.world.block.WalkRules;
import dev.helm.pathfinding.world.block.WorkCosts;
import dev.helm.setting.Settings;
import dev.helm.tools.MinedToolStrength;
import dev.helm.world.ClientLevelView;
import dev.helm.world.PlayerInventory;
import dev.helm.world.read.FrozenChunks;
import dev.helm.world.read.WorldBounds;

public final class SearchSegment {

    private final BlockView blocks;
    private final WalkRules walk;
    private final MoveEnvironment environment;
    private final MoveExpander[] expanders;

    private SearchSegment(BlockView blocks, WalkRules walk, MoveEnvironment environment,
                          MoveExpander[] expanders) {
        this.blocks = blocks;
        this.walk = walk;
        this.environment = environment;
        this.expanders = expanders;
    }

    public static SearchSegment capture(Set<Block> doNotBreak) {
        Minecraft client = Minecraft.getInstance();
        ClientLevel level = client.level;
        LocalPlayer player = client.player;
        if (level == null || player == null) {
            return null;
        }
        var cache = Settings.holder().cache();
        FrozenChunks chunks = FrozenChunks.capture(level.getChunkSource());
        BlockView blocks = new ClientLevelView(level, chunks,
                new WorldBounds(level.getWorldBorder()), cache.preferLoaded(),
                cache.enabled(), doNotBreak);
        Tunables tuning = TunablesFactory.fromSettings();
        WalkRules walk = new WalkRules(blocks, tuning);
        var strength = new MinedToolStrength(new PlayerInventory(player),
                Settings.holder().mining(), Settings.holder().movement());
        var work = new WorkCosts(blocks, walk, tuning, strength);
        MoveEnvironment environment = new FrozenEnvironment(blocks, walk, work, tuning);
        return new SearchSegment(blocks, walk, environment, Expanders.forEnvironment(environment));
    }

    public Search start(Goal goal, int fromX, int fromY, int fromZ, SearchBudget budget,
                        RouteFavor favor) {
        return new Search(fromX, fromY, fromZ, goal, environment, expanders, budget, favor);
    }

    public BlockView blocks() {
        return blocks;
    }

    public WalkRules walk() {
        return walk;
    }
}
