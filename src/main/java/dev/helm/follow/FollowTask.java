package dev.helm.follow;

import java.util.List;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;

import dev.helm.aim.Aim;
import dev.helm.aim.Aiming;
import dev.helm.aim.LookController;
import dev.helm.diag.Trace;
import dev.helm.farm.FarmTask;
import dev.helm.follow.standing.StandingGoal;
import dev.helm.follow.standing.Target;
import dev.helm.follow.standing.TargetWatch;
import dev.helm.follow.subject.FollowSubject;
import dev.helm.mine.MineTask;
import dev.helm.navigate.NavigatorAgent;
import dev.helm.navigate.Objective;
import dev.helm.navigate.Pilot;
import dev.helm.pathfinding.goal.Goal;
import dev.helm.pathfinding.search.SearchJob;
import dev.helm.setting.ClientNotice;
import dev.helm.setting.FollowSettings;
import dev.helm.setting.Settings;

public final class FollowTask {

    private static final FollowTask INSTANCE = new FollowTask();

    private boolean installed;
    private FollowSubject subject;
    private Target held;
    private int waiting;
    private int retry;
    private int replan;

    private FollowTask() {
    }

    public static FollowTask instance() {
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
        return subject != null;
    }

    public void start(FollowSubject wanted) {
        MineTask.instance().stop();
        FarmTask.instance().stop();
        NavigatorAgent.instance().pilot().forgetObjective();
        subject = wanted;
        held = null;
        waiting = 0;
        retry = 0;
        replan = 0;
        Trace.instance().barrier("follow");
        Trace.instance().event("follow", "following " + wanted.describe());
    }

    public void stop() {
        subject = null;
        held = null;
        waiting = 0;
        retry = 0;
        replan = 0;
        Pilot pilot = NavigatorAgent.instance().pilot();
        pilot.halt();
        pilot.forgetObjective();
        LookController.instance().clear();
    }

    public void onTick() {
        FollowSubject wanted = subject;
        if (wanted == null) {
            return;
        }
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        ClientLevel level = client.level;
        if (player == null || level == null || !NavigatorAgent.instance().navigator().ready()) {
            return;
        }

        FollowSettings settings = Settings.holder().follow();
        List<Target> targets = TargetWatch.scan(level, player, wanted, settings);
        if (targets.isEmpty()) {
            nothingMatched(wanted, settings);
            return;
        }
        waiting = 0;

        held = TargetWatch.sticky(TargetWatch.narrowed(targets, held, settings), held,
                settings.keepTarget());
        List<Target> wanted2 = held == null ? targets : List.of(held);
        StandingGoal standing = StandingGoal.towards(wanted2, settings);
        Trace.instance().pulse("follow-target", "follow", "following "
                + standing.target().label() + " from " + standing.spot().label()
                + ", " + targets.size() + " in range");

        if (standing.reached(player.getBlockX(), player.getBlockY(), player.getBlockZ())) {
            arrived(settings);
        } else {
            closeGap(standing, player, settings);
        }
        watch(standing.target(), player, settings);
    }

    private void nothingMatched(FollowSubject wanted, FollowSettings settings) {
        waiting++;
        held = null;
        if (waiting <= settings.waitTicks()) {
            Trace.instance().pulse("follow-waiting", "follow", "nothing matches "
                    + wanted.describe() + " yet, waiting tick " + waiting
                    + " of " + settings.waitTicks());
            return;
        }
        Trace.instance().event("follow", "nothing matched " + wanted.describe()
                + " for " + waiting + " ticks, so it is giving up");
        ClientNotice.warn("Nothing left to follow.");
        stop();
    }

    private void arrived(FollowSettings settings) {
        retry = 0;
        replan = 0;
        if (!settings.holdWhenClose()) {
            return;
        }
        Pilot pilot = NavigatorAgent.instance().pilot();
        if (pilot.isWalking() || pilot.searching()) {
            Trace.instance().event("follow", "close enough, so the walk is stopped");
            pilot.halt();
            pilot.forgetObjective();
        }
        pilot.holdStill();
    }

    private void closeGap(StandingGoal standing, LocalPlayer player, FollowSettings settings) {
        Pilot pilot = NavigatorAgent.instance().pilot();
        Goal goal = standing.goal();
        if (walkStillCounts(pilot, goal)) {
            replan = 0;
            return;
        }
        if (pilot.searching()) {
            return;
        }
        if (replan > 0) {
            replan--;
            return;
        }
        if (pilot.unreachable() && retry > 0) {
            retry--;
            Trace.instance().pulse("follow-blocked", "follow", "nothing walkable reaches "
                    + standing.spot().label() + " yet, looking again in " + retry + " ticks");
            return;
        }
        if (pilot.unreachable()) {
            Trace.instance().event("follow", "nothing walkable reached "
                    + standing.spot().label() + ", so it is looking again");
        }
        SearchJob job = NavigatorAgent.instance().navigator()
                .searchFor(goal, player.getBlockX(), player.getBlockY(), player.getBlockZ(),
                        tuning -> tuning
                                .allowingWork(settings.breakBlocks(), settings.placeBlocks())
                                .allowingSprint(settings.sprint()));
        if (job == null) {
            return;
        }
        pilot.await(job, Objective.silently(goal, standing.spot().label()));
        retry = settings.retryTicks();
        replan = settings.replanTicks();
        Trace.instance().event("follow", "searching for a way to " + standing.spot().label()
                + " to stay near " + standing.target().label());
    }

    private boolean walkStillCounts(Pilot pilot, Goal goal) {
        int[] destination = pilot.route().destination();
        if (destination == null) {
            return false;
        }
        if (goal.reached(destination[0], destination[1], destination[2])) {
            retry = 0;
            pilot.forgetObjective();
            return true;
        }
        Trace.instance().event("follow", "the route was aiming at " + destination[0] + " "
                + destination[1] + " " + destination[2] + ", which no longer counts, "
                + "so it is searching again");
        pilot.halt();
        pilot.forgetObjective();
        return false;
    }

    private void watch(Target target, LocalPlayer player, FollowSettings settings) {
        if (!settings.lookAtTarget() || NavigatorAgent.instance().pilot().isWalking()) {
            return;
        }
        var entity = target.entity();
        Aim aim = Aiming.lookFrom(player.getX(), player.getEyeY(), player.getZ(),
                entity.getX(), entity.getEyeY(), entity.getZ());
        double limit = settings.maxLookPitch();
        Aim wanted = aim.withPitch(Math.max(-limit, Math.min(limit, aim.pitch())));
        LookController.instance().watch(Aiming.shortestFrom(LookController.instance().effective(),
                wanted));
    }
}