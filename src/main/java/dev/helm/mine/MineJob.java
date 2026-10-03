package dev.helm.mine;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;

import dev.helm.mine.drops.DropLedger;
import dev.helm.mine.drops.DroppedTargets;
import dev.helm.mine.drops.HeldTargets;
import dev.helm.mine.find.CacheTargets;
import dev.helm.mine.find.TargetPruner;
import dev.helm.mine.goal.ExploreAwayGoal;
import dev.helm.mine.goal.SpotGoals;
import dev.helm.mine.shaft.ColumnRise;
import dev.helm.mine.shaft.ColumnRiser;
import dev.helm.mine.target.TargetFilter;
import dev.helm.mine.vein.Vein;
import dev.helm.pathfinding.goal.AnyGoal;
import dev.helm.pathfinding.goal.Goal;
import dev.helm.pathfinding.world.BlockView;
import dev.helm.pathfinding.world.block.WorkCosts;
import dev.helm.setting.MiningSettings;
import dev.helm.setting.Settings;
import dev.helm.world.read.ChunkScanRequest;
import dev.helm.world.read.Sweep;

public final class MineJob {

    private final TargetFilter filter;
    private final int wanted;
    private final List<BlockPos> known = new ArrayList<>();
    private final List<BlockPos> unreachable = new ArrayList<>();
    private final DropLedger ledger = new DropLedger();

    private static final int COLLECT_RADIUS_SQUARED = 64;

    private BlockPos startPoint;
    private BlockPos routeFor;
    private List<BlockPos> working;
    private Sweep sweep;
    private List<BlockPos> swept = List.of();
    private long sweepBegan;
    private int sweepTicks;
    private int sinceRescan;
    private boolean searched;

    private final String rawRequest;

    public MineJob(TargetFilter filter, int wanted, String rawRequest) {
        this.filter = filter;
        this.wanted = wanted;
        this.rawRequest = rawRequest;
    }

    public int wanted() {
        return wanted;
    }

    public String rawRequest() {
        return rawRequest;
    }

    public TargetFilter filter() {
        return filter;
    }

    public List<BlockPos> known() {
        return known;
    }

    public List<BlockPos> unreachable() {
        return unreachable;
    }

    public DropLedger ledger() {
        return ledger;
    }

    public boolean sweeping() {
        return sweep != null;
    }

    public boolean enoughCarried(LocalPlayer player) {
        return wanted > 0 && HeldTargets.carried(player, filter) >= wanted;
    }

    public int carried(LocalPlayer player) {
        return HeldTargets.carried(player, filter);
    }

    public void hold(BlockPos pos, MiningSettings settings) {
        ledger.hold(pos, System.currentTimeMillis() + settings.dropWaitMillis());
    }

    public void expireDrops() {
        ledger.expire(System.currentTimeMillis());
    }

    public List<BlockPos> dropsIn(ClientLevel level) {
        if (!Settings.holder().mining().followDroppedItems()) {
            return ledger.positions();
        }
        return DroppedTargets.gather(level, filter);
    }

    public void beginSweep(ClientLevel level, BlockPos feet, MiningSettings settings) {
        if (sweep != null) {
            return;
        }
        sweep = Sweep.around(level, feet, new ChunkScanRequest(filter.blocks(),
                settings.maxTargets(), settings.scanRadius(), settings.scanLevelWindow()));
        swept = List.of();
        sweepBegan = System.nanoTime();
        sweepTicks = 0;
    }

    public void stepSweep(long budgetNanos) {
        if (sweep == null) {
            return;
        }
        sweepTicks++;
        if (sweep.step(budgetNanos)) {
            swept = sweep.found();
            sweep = null;
        }
    }

    public int sweptCount() {
        return swept.size();
    }

    public long sweepMillis() {
        return (System.nanoTime() - sweepBegan) / 1_000_000L;
    }

    public int sweepTicks() {
        return sweepTicks;
    }

    public boolean dueForSweep(MiningSettings settings) {
        if (settings.rescanEveryTicks() <= 0) {
            return false;
        }
        boolean due = sinceRescan % settings.rescanEveryTicks() == 0;
        sinceRescan++;
        return due;
    }

    public void compose(ClientLevel level, BlockPos feet, BlockView world, WorkCosts work,
                        MiningSettings settings, List<BlockPos> drops) {
        List<BlockPos> found = new ArrayList<>(swept);
        found.addAll(CacheTargets.around(filter, feet,
                settings.cacheScanLimit(), settings.cacheScanRadius()));
        found.addAll(known);
        found.addAll(drops);
        replace(found, world, work, settings, feet, drops);
        searched = true;
    }

    public void markSearched() {
        searched = true;
    }

    public void prune(BlockView world, WorkCosts work, MiningSettings settings, BlockPos feet,
                      List<BlockPos> drops) {
        if (known.isEmpty()) {
            return;
        }
        List<BlockPos> found = new ArrayList<>(known);
        found.addAll(drops);
        replace(found, world, work, settings, feet, drops);
    }

    private void replace(List<BlockPos> found, BlockView world, WorkCosts work,
                         MiningSettings settings, BlockPos feet, List<BlockPos> drops) {
        List<BlockPos> kept = TargetPruner.prune(found, filter, world, work, settings, feet,
                unreachable, drops, settings.maxTargets());
        known.clear();
        known.addAll(kept);
    }

    public Goal goal(BlockView world, WorkCosts work, MiningSettings settings, BlockPos feet,
                     LocalPlayer player, List<BlockPos> drops) {
        routeFor = null;
        ColumnRise rise = rising(world, settings, feet, player);
        if (rise != null) {
            routeFor = rise.target();
            return rise.stand();
        }
        List<BlockPos> solid = committed(world);
        if (!solid.isEmpty()) {
            List<Goal> goals = new ArrayList<>(solid.size());
            for (BlockPos pos : solid) {
                goals.add(SpotGoals.forPosition(pos, solid, filter, world, work, settings));
            }
            routeFor = solid.get(0);
            return AnyGoal.of(goals);
        }
        if (settings.waitForDrops() && !drops.isEmpty()) {
            Goal collecting = hold(world, work, settings, feet, drops);
            if (collecting != null) {
                return collecting;
            }
        }
        working = null;
        return wander(settings, feet);
    }

    private Goal hold(BlockView world, WorkCosts work, MiningSettings settings,
                      BlockPos feet, List<BlockPos> drops) {
        List<Goal> goals = new ArrayList<>(drops.size());
        for (BlockPos pos : drops) {
            if (pos.distSqr(feet) > COLLECT_RADIUS_SQUARED) {
                continue;
            }
            goals.add(SpotGoals.forPosition(pos, drops, filter, world, work, settings));
        }
        routeFor = null;
        return goals.isEmpty() ? null : AnyGoal.of(goals);
    }

    public List<BlockPos> committed(BlockView world) {
        List<BlockPos> standing = vein(world);
        if (standing.isEmpty()) {
            return standing;
        }
        BlockPos seed = standing.get(0);
        if (working == null) {
            working = Vein.around(standing, world, filter, seed);
        }
        List<BlockPos> left = new ArrayList<>();
        for (BlockPos pos : working) {
            if (filter.wants(world.stateAt(pos.getX(), pos.getY(), pos.getZ()))) {
                left.add(pos);
            }
        }
        return left;
    }

    public List<BlockPos> vein(BlockView world) {
        List<BlockPos> found = new ArrayList<>();
        for (BlockPos pos : known) {
            if (filter.wants(world.stateAt(pos.getX(), pos.getY(), pos.getZ()))) {
                found.add(pos);
            }
        }
        return found;
    }

    private ColumnRise rising(BlockView world, MiningSettings settings, BlockPos feet,
                              LocalPlayer player) {
        if (!settings.pillarToReach() || known.isEmpty()) {
            return null;
        }
        if (!Settings.holder().movement().allowPlace()
                || !Settings.holder().movement().allowBreak()) {
            return null;
        }
        return ColumnRiser.find(known, world, feet, player, Settings.holder().look());
    }

    private Goal wander(MiningSettings settings, BlockPos feet) {
        if (!searched) {
            return null;
        }
        if (!settings.sightOnly() && !settings.exploreWhenUnknown()) {
            return null;
        }
        if (startPoint == null) {
            startPoint = feet;
        }
        return new ExploreAwayGoal(startPoint.getX(), startPoint.getZ());
    }

    public boolean routeTargetStillKnown() {
        return routeFor == null || known.contains(routeFor);
    }

    public boolean forgetClosest(BlockPos feet) {
        BlockPos closest = null;
        for (BlockPos pos : known) {
            if (closest == null || pos.distSqr(feet) < closest.distSqr(feet)) {
                closest = pos;
            }
        }
        if (closest == null) {
            return false;
        }
        unreachable.add(closest);
        known.remove(closest);
        return true;
    }
}