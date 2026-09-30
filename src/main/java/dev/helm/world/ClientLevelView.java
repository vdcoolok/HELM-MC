package dev.helm.world;

import java.util.EnumSet;
import java.util.Set;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import dev.helm.pathfinding.world.BlockView;

public final class ClientLevelView implements BlockView {

    private final ClientLevel level;
    private final Set<Block> doNotBreak;

    public ClientLevelView(ClientLevel level) {
        this(level, java.util.Collections.emptySet());
    }

    public ClientLevelView(ClientLevel level, Set<Block> doNotBreak) {
        this.level = level;
        this.doNotBreak = doNotBreak;
    }

    public ClientLevel level() {
        return level;
    }

    public Set<Block> doNotBreak() {
        return doNotBreak;
    }

    @Override
    public BlockState stateAt(int x, int y, int z) {
        return LevelView.stateAt(level, x, y, z);
    }

    @Override
    public boolean loaded(int x, int z) {
        return LevelView.chunkLoaded(level, x, z);
    }

    @Override
    public int lowestLevel() {
        return LevelView.lowestLevel(level);
    }

    @Override
    public int levelCount() {
        return LevelView.levelCount(level);
    }

    @Override
    public boolean insideBorder(int x, int y, int z) {
        return LevelView.insideBorder(level, x, y, z);
    }
}
