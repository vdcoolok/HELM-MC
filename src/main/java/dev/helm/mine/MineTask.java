package dev.helm.mine;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import dev.helm.aim.Aim;
import dev.helm.aim.AimTrace;
import dev.helm.aim.LookController;
import dev.helm.diag.Trace;
import dev.helm.farm.FarmTask;
import dev.helm.mine.drops.DropWatch;
import dev.helm.mine.shaft.OverheadFinder;
import dev.helm.mine.shaft.OverheadMiner;
import dev.helm.mine.shaft.OverheadSpot;
import dev.helm.mine.target.TargetFilter;
import dev.helm.navigate.NavigatorAgent;
import dev.helm.navigate.Objective;
import dev.helm.navigate.Pilot;
import dev.helm.pathfinding.goal.Goal;
import dev.helm.pathfinding.search.SearchJob;
import dev.helm.pathfinding.world.BlockView;
import dev.helm.pathfinding.world.block.WorkCosts;
import dev.helm.setting.ClientNotice;
import dev.helm.setting.MiningSettings;
import dev.helm.setting.Settings;

public final class MineTask {

    private static final long SWEEP_BUDGET_NANOS = 4_000_000L;
    private static final int SETTLE_TICKS = 20;
    private static final int SEARCH_GRACE_TICKS = 120;

    private static final MineTask INSTANCE = new MineTask();

    private boolean installed;
    private MineJob job;
    private BlockPos overhead;
    private int settling;
    private int awaiting;

    private MineTask() {
    }

    public static MineTask instance() {
        return INSTANCE;
    }

    public void install() {
        if (installed) {
            return;
        }
        installed = true;
        ClientTickEvents.END_CLIENT_TICK.register(client -> INSTANCE.onTick());
    }

    public boolean running() {
        return job != null;
    }

    public List<BlockPos> targets() {
        return job == null ? List.of() : List.copyOf(job.known());
    }

    public BlockPos breaking() {
        return overhead;
    }

    public void start(TargetFilter filter, int wanted, String rawRequest) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        FarmTask.instance().stop();
        job = new MineJob(filter, wanted, rawRequest);
        Trace.instance().barrier("mine");
        Trace.instance().event("mine", "mining " + filter.describe()
                + (wanted > 0 ? " until " + wanted + " are carried" : " with no limit"));
        job.beginSweep(Minecraft.getInstance().level, player.blockPosition(),
                Settings.holder().mining());
    }

    public void stop() {
        job = null;
        overhead = null;
        settling = 0;
        awaiting = 0;
        release();
    }

    public void onTick() {
        MineJob current = job;
        if (current == null) {
            return;
        }
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        ClientLevel level = client.level;
        NavigatorAgent agent = NavigatorAgent.instance();
        if (player == null || level == null || !agent.navigator().ready()) {
            return;
        }
        Pilot pilot = agent.pilot();
        BlockView world = agent.navigator().blocks();
        WorkCosts work = agent.navigator().work();
        MiningSettings settings = Settings.holder().mining();
        BlockPos feet = player.blockPosition();

        if (current.enoughCarried(player)) {
            collected(current, player);
            return;
        }

        Trace.instance().pulse("mine-known", "mine", "raw=[" + current.rawRequest()
                + "] wanted=" + current.wanted() + " carrying=" + current.carried(player)
                + " swept=" + current.sweptCount() + " known=" + current.known().size()
                + " nearest=" + describe(current.known(), feet, world));
        List<BlockPos> drops = gatherDrops(current, level, world, player, settings);

        sweep(current, level, feet, world, work, settings, drops);
        if (settings.sightOnly()) {
            VisibleTargets.refresh(current, level, world, work, settings, player, feet);
            current.markSearched();
        }
        current.prune(world, work, settings, feet, drops);

        if (breakOverhead(current, world, work, settings, player)) {
            return;
        }
        if (pilot.isWalking()) {
            dropStaleRoute(pilot, current, settings);
            return;
        }
        if (pilot.searching()) {
            if (++awaiting > SEARCH_GRACE_TICKS) {
                awaiting = 0;
                pilot().halt();
                Trace.instance().event("mine", "a search made no progress for "
                        + SEARCH_GRACE_TICKS + " ticks, dropping it and looking again");
            }
            return;
        }
        awaiting = 0;
        if (dropsLanding(current, level, settings)) {
            pilot().holdStill();
            return;
        }
        if (pilot.unreachable() && !unreachable(current, feet, settings)) {
            return;
        }
        headForNext(current, world, work, settings, feet, player, drops);
    }

    private boolean dropsLanding(MineJob current, ClientLevel level, MiningSettings settings) {
        if (!settings.waitForDrops()) {
            return false;
        }
        for (var entity : DropWatch.matching(level, current.filter())) {
            if (DropWatch.stillFalling(entity)) {
                Trace.instance().pulse("mine-landing", "mine",
                        "waiting for a drop to finish falling");
                return true;
            }
        }
        return false;
    }

    private void headForNext(MineJob current, BlockView world, WorkCosts work,
                             MiningSettings settings, BlockPos feet, LocalPlayer player,
                             List<BlockPos> drops) {
        Goal goal = current.goal(world, work, settings, feet, player, drops);
        if (goal == null) {
            if (!current.sweeping()) {
                outOfWork(current);
            }
            return;
        }
        SearchJob search = NavigatorAgent.instance().navigator()
                .searchFor(goal, feet.getX(), feet.getY(), feet.getZ());
        if (search == null) {
            outOfWork(current);
            return;
        }
        pilot().await(search, new Objective(goal, "the next " + current.filter().describe()));
    }

    private boolean breakOverhead(MineJob current, BlockView world, WorkCosts work,
                                 MiningSettings settings, LocalPlayer player) {
        if (!settings.breakOverhead()) {
            overhead = null;
            return false;
        }
        BlockPos held = overhead;
        if (held == null || !stillAbove(held, world, work, player)) {
            held = null;
            OverheadSpot spot = OverheadFinder.find(current.known(), world, work, player);
            held = spot == null ? null : spot.block();
        }
        if (held == null) {
            overhead = null;
            settling = 0;
            return false;
        }
        overhead = held;
        BlockPos feet = player.blockPosition();
        pilot().stopWalking();
        boolean striking = OverheadMiner.work(pilot(),
                new OverheadSpot(held, feet.getX(), feet.getY(), feet.getZ()), player,
                Settings.holder().look());
        pilot().holdStill();
        if (striking) {
            settling = 0;
        } else if (++settling > SETTLE_TICKS) {
            overhead = null;
            settling = 0;
            return false;
        }
        Trace.instance().repeat("mine-overhead", "mine", "breaking the block above at "
                + held.getX() + " " + held.getY() + " " + held.getZ() + " without moving");
        return true;
    }

    private boolean stillAbove(BlockPos pos, BlockView world, WorkCosts work,
                               LocalPlayer player) {
        BlockPos feet = player.blockPosition();
        if (!player.onGround() || pos.getX() != feet.getX() || pos.getZ() != feet.getZ()) {
            return false;
        }
        if (pos.getY() < feet.getY()) {
            return false;
        }
        if (world.stateAt(pos.getX(), pos.getY(), pos.getZ()).getBlock() instanceof AirBlock) {
            return false;
        }
        return !work.avoidBreaking(pos.getX(), pos.getY(), pos.getZ(),
                world.stateAt(pos.getX(), pos.getY(), pos.getZ()));
    }

    private void sweep(MineJob current, ClientLevel level, BlockPos feet, BlockView world,
                       WorkCosts work, MiningSettings settings, List<BlockPos> drops) {
        if (!settings.sightOnly() && current.dueForSweep(settings)) {
            current.beginSweep(level, feet, settings);
        }
        if (!current.sweeping()) {
            return;
        }
        current.stepSweep(SWEEP_BUDGET_NANOS);
        if (current.sweeping()) {
            return;
        }
        Trace.instance().event("mine", "looked around over " + current.sweepTicks()
                + " ticks and " + current.sweepMillis() + "ms");
        current.compose(level, feet, world, work, settings, drops);
    }

    private List<BlockPos> gatherDrops(MineJob current, ClientLevel level, BlockView world,
                                    LocalPlayer player, MiningSettings settings) {
        current.expireDrops();
        Set<BlockPos> held = new LinkedHashSet<>(current.ledger().positions());
        BlockPos looked = lookedAt(player);
        if (looked != null && current.known().contains(looked) && standing(world, looked)) {
            current.hold(looked, settings);
            held.add(looked);
        }
        held.addAll(current.dropsIn(level));
        return List.copyOf(held);
    }

    private boolean standing(BlockView world, BlockPos pos) {
        return !(world.stateAt(pos.getX(), pos.getY(), pos.getZ()).getBlock() instanceof AirBlock);
    }

    private String describe(List<BlockPos> known, BlockPos feet, BlockView world) {
        BlockPos closest = null;
        for (BlockPos pos : known) {
            if (closest == null || pos.distSqr(feet) < closest.distSqr(feet)) {
                closest = pos;
            }
        }
        return closest == null ? "nothing"
                : closest.getX() + " " + closest.getY() + " " + closest.getZ()
                    + " at " + (int) Math.sqrt(closest.distSqr(feet)) + " blocks holding "
                    + world.stateAt(closest.getX(), closest.getY(), closest.getZ())
                        .getBlock().getName().getString();
    }

    private BlockPos lookedAt(LocalPlayer player) {
        Aim aim = LookController.instance().effective();
        if (aim == null) {
            return null;
        }
        HitResult trace = AimTrace.towards(player, aim,
                Settings.holder().look().blockReachDistance(), player.isCrouching());
        if (trace == null || trace.getType() != HitResult.Type.BLOCK) {
            return null;
        }
        return ((BlockHitResult) trace).getBlockPos();
    }

    private void dropStaleRoute(Pilot pilot, MineJob current, MiningSettings settings) {
        if (!settings.stopRouteWhenMined() || current.routeTargetStillKnown()) {
            return;
        }
        int[] destination = pilot.route().destination();
        if (destination == null) {
            return;
        }
        Trace.instance().event("mine", "the block the route was heading for at "
                + destination[0] + " " + destination[1] + " " + destination[2]
                + " has gone, stopping the walk there");
        pilot.halt();
    }

    private boolean unreachable(MineJob current, BlockPos feet, MiningSettings settings) {
        if (!settings.skipUnreachable() || !current.forgetClosest(feet)) {
            ClientNotice.warn("No way to reach any of " + current.filter().describe() + ".");
            giveUp(current);
            return false;
        }
        Trace.instance().event("mine", "no route to any target, marking the closest one "
                + "unreachable and trying the next");
        release();
        return true;
    }

    private void collected(MineJob current, LocalPlayer player) {
        ClientNotice.warn("Have " + current.carried(player) + " of "
                + current.filter().describe() + ".");
        giveUp(current);
    }

    private void outOfWork(MineJob current) {
        Trace.instance().event("mine", "nothing left that is worth mining for "
                + current.filter().describe());
        ClientNotice.warn("No " + current.filter().describe() + " left to mine.");
        giveUp(current);
    }

    private void giveUp(MineJob current) {
        release();
        overhead = null;
        settling = 0;
        awaiting = 0;
        Trace.instance().event("mine", "stopped mining " + current.filter().describe());
        job = null;
    }

    private void release() {
        Pilot pilot = pilot();
        pilot.halt();
        pilot.forgetObjective();
    }

    private static Pilot pilot() {
        return NavigatorAgent.instance().pilot();
    }
}