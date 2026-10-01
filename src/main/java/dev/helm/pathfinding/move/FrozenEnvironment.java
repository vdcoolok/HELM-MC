package dev.helm.pathfinding.move;

import dev.helm.pathfinding.context.Tunables;
import dev.helm.pathfinding.world.BlockView;
import dev.helm.pathfinding.world.block.WalkRules;
import dev.helm.pathfinding.world.block.WorkCosts;
import net.minecraft.world.level.block.state.BlockState;

public final class FrozenEnvironment implements MoveEnvironment {

    private final BlockView view;
    private final WalkRules walk;
    private final WorkCosts work;
    private final Tunables tuning;

    public FrozenEnvironment(BlockView view, WalkRules walk, WorkCosts work, Tunables tuning) {
        this.view = view;
        this.walk = walk;
        this.work = work;
        this.tuning = tuning;
    }

    @Override
    public WalkRules walk() {
        return walk;
    }

    @Override
    public WorkCosts work() {
        return work;
    }

    @Override
    public Tunables tuning() {
        return tuning;
    }

    @Override
    public BlockState stateAt(int x, int y, int z) {
        return view.stateAt(x, y, z);
    }

    @Override
    public boolean loaded(int x, int z) {
        return view.loaded(x, z);
    }

    @Override
    public boolean residentChunk(int x, int z) {
        return view.residentChunk(x, z);
    }

    @Override
    public int lowestLevel() {
        return view.lowestLevel();
    }

    @Override
    public int levelCount() {
        return view.levelCount();
    }

    @Override
    public boolean entirelyInsideBorder(int x, int z) {
        return view.entirelyInsideBorder(x, z);
    }

    @Override
    public boolean canPlaceAt(int x, int z) {
        return view.canPlaceAt(x, z);
    }
}