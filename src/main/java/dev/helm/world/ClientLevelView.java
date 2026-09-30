package dev.helm.world;

import java.util.Collections;
import java.util.Set;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import dev.helm.pathfinding.world.BlockView;
import dev.helm.world.read.BlockReader;
import dev.helm.world.read.WorldBounds;

public final class ClientLevelView implements BlockView {

    private final ClientLevel level;
    private final BlockReader reader;
    private final WorldBounds bounds;
    private final Set<Block> doNotBreak;

    public ClientLevelView(ClientLevel level) {
        this(level, Collections.emptySet());
    }

    public ClientLevelView(ClientLevel level, Set<Block> doNotBreak) {
        this.level = level;
        this.reader = new BlockReader(level);
        this.bounds = new WorldBounds(level.getWorldBorder());
        this.doNotBreak = doNotBreak;
    }

    public ClientLevel level() {
        return level;
    }

    public BlockReader reader() {
        return reader;
    }

    public Set<Block> doNotBreak() {
        return doNotBreak;
    }

    @Override
    public BlockState stateAt(int x, int y, int z) {
        return reader.state(x, y, z);
    }

    @Override
    public boolean loaded(int x, int z) {
        return reader.loaded(x, z);
    }

    @Override
    public boolean residentChunk(int x, int z) {
        return reader.residentChunk(x, z);
    }

    @Override
    public int lowestLevel() {
        return reader.range().lowestLevel();
    }

    @Override
    public int levelCount() {
        return reader.range().levelCount();
    }

    @Override
    public boolean entirelyInsideBorder(int x, int z) {
        return bounds.entirelyContains(x, z);
    }

    @Override
    public boolean canPlaceAt(int x, int z) {
        return bounds.canPlaceAt(x, z);
    }
}
