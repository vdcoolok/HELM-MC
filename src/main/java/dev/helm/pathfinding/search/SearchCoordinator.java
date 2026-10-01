package dev.helm.pathfinding.search;

import java.util.Set;

import net.minecraft.world.level.block.Block;

import dev.helm.pathfinding.goal.Goal;
import dev.helm.setting.Settings;

public final class SearchCoordinator {

    private final SearchPool pool = new SearchPool();

    private SearchJob inProgress;

    public boolean searching() {
        return inProgress != null;
    }

    public SearchJob submit(Goal goal, int fromX, int fromY, int fromZ, Set<Block> doNotBreak,
                            SegmentBudget millis, RouteFavor favor) {
        cancel();

        SearchSegment segment = SearchSegment.capture(doNotBreak);
        if (segment == null) {
            return null;
        }

        Search search = segment.start(goal, fromX, fromY, fromZ,
                millis.with(Settings.holder().path()), favor);
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

    public void shutdown() {
        cancel();
        pool.shutdown();
    }
}
