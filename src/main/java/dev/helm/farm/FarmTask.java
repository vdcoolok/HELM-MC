package dev.helm.farm;

import java.util.List;
import java.util.function.Predicate;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.AirBlock;

import dev.helm.aim.Aim;
import dev.helm.aim.LookController;
import dev.helm.control.Control;
import dev.helm.diag.Trace;
import dev.helm.navigate.NavigatorAgent;
import dev.helm.navigate.Objective;
import dev.helm.navigate.Pilot;
import dev.helm.pathfinding.goal.AnyGoal;
import dev.helm.pathfinding.search.SearchJob;
import dev.helm.setting.ClientNotice;
import dev.helm.setting.LookSettings;
import dev.helm.setting.Settings;
import dev.helm.tools.ToolChooser;
import dev.helm.world.read.Sweep;

public final class FarmTask {

    private static final long SWEEP_BUDGET_NANOS = 4_000_000L;

    private static final FarmTask INSTANCE = new FarmTask();

    private boolean installed;
    private boolean running;
    private boolean announced;

    private final HarvestLedger harvested = new HarvestLedger();
    private List<BlockPos> wanted = List.of();

    private FarmArea area = FarmArea.everywhere(BlockPos.ZERO);
    private List<BlockPos> swept = List.of();
    private Sweep sweeping;
    private long sweepingSince;
    private int sweepingSteps;
    private int ticks;

    public static FarmTask instance() {
        return INSTANCE;
    }

    public void install() {
        if (installed) {
            return;
        }
        installed = true;
        ClientTickEvents.END_CLIENT_TICK.register(client -> INSTANCE.onTick());
    }

    public void start(FarmArea around) {
        this.area = around;
        this.swept = List.of();
        this.sweeping = null;
        this.sweepingSteps = 0;
        this.ticks = 0;
        this.announced = false;
        this.running = true;
        this.wanted = List.of();
        this.harvested.clear();
        Trace.instance().barrier("farm");
        Trace.instance().event("farm", "farming " + around.describe() + " from "
                + around.centre().getX() + " " + around.centre().getY() + " "
                + around.centre().getZ());
    }

    public void stop() {
        this.running = false;
        this.swept = List.of();
        this.sweeping = null;
        this.wanted = List.of();
        this.harvested.clear();
    }

    public boolean running() {
        return running;
    }

    public List<BlockPos> harvestable() {
        return wanted;
    }

    public HarvestLedger harvestLedger() {
        return harvested;
    }

    public void onTick() {
        if (!running) {
            return;
        }
        var client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        ClientLevel level = client.level;
        if (player == null || level == null) {
            stop();
            return;
        }

        sweepInTime(level, player);
        List<BlockPos> seen = sweeping != null ? sweeping.found() : swept;
        FarmFindings findings = FarmSurvey.classify(level, seen, area);
        wanted = findings.harvestable();
        if (tended(findings, player, level)) {
            return;
        }
        if (sweeping != null && seen.isEmpty()) {
            return;
        }
        if (pilot().unreachable()) {
            giveUp("nothing it wanted could be reached");
            return;
        }
        headFor(findings, player, level);
    }

    private void sweepInTime(ClientLevel level, LocalPlayer player) {
        if (sweeping != null) {
            sweepingSteps++;
            if (sweeping.step(SWEEP_BUDGET_NANOS)) {
                swept = sweeping.found();
                Trace.instance().event("farm", "swept and found " + swept.size()
                        + " blocks worth looking at in " + millisSince(sweepingSince)
                        + "ms over " + sweepingSteps + " ticks");
                sweeping = null;
                sweepingSteps = 0;
            }
            return;
        }
        int interval = Settings.holder().farm().rescanEveryTicks();
        boolean due = interval <= 0 ? ticks == 0 : ticks % interval == 0;
        ticks++;
        if (!due) {
            return;
        }
        Pilot pilot = pilot();
        if (pilot.isWalking() || pilot.searching()) {
            return;
        }
        sweeping = FarmScan.begin(level, scanCentre(player), Settings.holder().farm());
        sweepingSince = System.nanoTime();
        sweepingSteps = 0;
    }

    private boolean tended(FarmFindings findings, LocalPlayer player, ClientLevel level) {
        return harvestNearby(findings, player, level)
                || sowNearby(findings, player)
                || coatNearby(findings, player, level)
                || feedNearby(findings, player);
    }

    private boolean harvestNearby(FarmFindings findings, LocalPlayer player, ClientLevel level) {
        for (BlockPos crop : findings.harvestable()) {
            if (!FarmHands.inReach(player, crop, look())) {
                continue;
            }
            Aim aim = FarmHands.atBlock(player, crop, look());
            if (aim == null) {
                continue;
            }
            ToolChooser.forBlock(crop);
            harvested.record(level.getBlockState(crop).getBlock());
            return tendTo(player, crop, aim, Control.ATTACK, true);
        }
        return false;
    }

    private boolean sowNearby(FarmFindings findings, LocalPlayer player) {
        for (BlockPos soil : findings.bareFarmland()) {
            if (sow(player, soil, FarmItems::plantable)) {
                return true;
            }
        }
        for (BlockPos soil : findings.bareSoulSand()) {
            if (sow(player, soil, FarmItems::netherWart)) {
                return true;
            }
        }
        return false;
    }

    private boolean sow(LocalPlayer player, BlockPos soil, Predicate<ItemStack> wanted) {
        if (!FarmHands.inReach(player, soil, look()) || !FarmCarrying.ready(player, wanted)) {
            return false;
        }
        Aim aim = FarmHands.atTopOf(player, soil, look());
        if (aim == null || !FarmHands.facingSide(aim, Direction.UP, player, look())) {
            return false;
        }
        return tendTo(player, soil, aim, Control.USE, false);
    }

    private boolean coatNearby(FarmFindings findings, LocalPlayer player, ClientLevel level) {
        for (BlockPos log : findings.bareLogs()) {
            if (!FarmHands.inReach(player, log, look())) {
                continue;
            }
            for (Direction side : Direction.Plane.HORIZONTAL) {
                if (!(level.getBlockState(log.relative(side)).getBlock() instanceof AirBlock)) {
                    continue;
                }
                Aim aim = FarmHands.atSideOf(player, log, side, look());
                if (aim == null || !FarmHands.facingSide(aim, side, player, look())) {
                    continue;
                }
                if (!FarmCarrying.ready(player, FarmItems::cocoaBeans)) {
                    continue;
                }
                return tendTo(player, log, aim, Control.USE, false);
            }
        }
        return false;
    }

    private boolean feedNearby(FarmFindings findings, LocalPlayer player) {
        for (BlockPos plant : findings.wantsBoneMeal()) {
            if (!FarmHands.inReach(player, plant, look())) {
                continue;
            }
            Aim aim = FarmHands.atBlock(player, plant, look());
            if (aim == null || !FarmCarrying.ready(player, FarmItems::boneMeal)) {
                continue;
            }
            return tendTo(player, plant, aim, Control.USE, false);
        }
        return false;
    }

    private boolean tendTo(LocalPlayer player, BlockPos target, Aim aim, Control control,
                            boolean breaking) {
        standStill();
        LookController.instance().aimAt(aim, true);
        boolean onTarget = breaking
                ? FarmHands.crosshairOnBlock(player, target, look())
                : FarmHands.handOnBlock(player, target, look());
        if (onTarget) {
            pilot().controls().set(control, true);
            Trace.instance().repeat("farm-tend", "farm", "working on " + target.getX() + " "
                    + target.getY() + " " + target.getZ());
        }
        return true;
    }

    private void headFor(FarmFindings findings, LocalPlayer player, ClientLevel level) {
        Pilot pilot = pilot();
        if (pilot.isWalking() || pilot.searching()) {
            return;
        }
        long started = System.nanoTime();
        AnyGoal goal = FarmGoals.from(findings, player, level);
        Trace.instance().event("farm", "built a goal of " + goal.count() + " targets in "
                + millisSince(started) + "ms");
        if (goal.count() == 0) {
            outOfWork();
            return;
        }
        var feet = pilot().feet();
        if (feet == null) {
            giveUp("the player is not in a world");
            return;
        }
        SearchJob job = NavigatorAgent.instance().navigator()
                .searchFor(goal, feet[0], feet[1], feet[2]);
        if (job == null) {
            giveUp("the search could not be started");
            return;
        }
        announced = false;
        pilot.await(job, new Objective(goal, "the next farm job"));
    }

    private void outOfWork() {
        standStill();
        if (announced) {
            return;
        }
        announced = true;
        Trace.instance().event("farm", "nothing ripe to do, waiting and watching the field");
        ClientNotice.warn("Nothing to harvest right now. Still watching.");
    }

    private void standStill() {
        pilot().holdStill();
    }

    private BlockPos scanCentre(LocalPlayer player) {
        int[] feet = pilot().feet();
        return feet == null ? player.blockPosition()
                : new BlockPos(feet[0], feet[1], feet[2]);
    }

    private void giveUp(String why) {
        Trace.instance().event("farm", "giving up because " + why);
        ClientNotice.warn("Farm failed.");
        Pilot pilot = pilot();
        pilot.halt();
        pilot.forgetObjective();
        stop();
    }

    private static Pilot pilot() {
        return NavigatorAgent.instance().pilot();
    }

    private static long millisSince(long started) {
        return (System.nanoTime() - started) / 1_000_000L;
    }

    private static LookSettings look() {
        return Settings.holder().look();
    }
}