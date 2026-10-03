package dev.helm.follow.standing;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;

import dev.helm.follow.subject.FollowSubject;
import dev.helm.setting.FollowSettings;

public final class TargetWatch {

    private TargetWatch() {
    }

    public static List<Target> scan(ClientLevel level, LocalPlayer player, FollowSubject subject,
                                    FollowSettings settings) {
        List<Target> found = new ArrayList<>();
        if (level == null || player == null || subject == null) {
            return found;
        }
        int nearest = settings.minTargetDistance();
        int nearLimit = nearest <= 0 ? 0 : nearest * nearest;
        int farLimit = settings.maxTargetDistance() <= 0
                ? Integer.MAX_VALUE
                : settings.maxTargetDistance() * settings.maxTargetDistance();
        for (Entity entity : level.entitiesForRendering()) {
            double between = player.distanceToSqr(entity);
            if (between < nearLimit || between > farLimit) {
                continue;
            }
            if (!eligible(entity, player, subject)) {
                continue;
            }
            found.add(Target.of(entity, player.distanceTo(entity)));
        }
        return found;
    }

    public static Target sticky(List<Target> narrowed, Target held, boolean keepTarget) {
        if (!keepTarget || held == null) {
            return closest(narrowed);
        }
        for (Target target : narrowed) {
            if (target.entity() == held.entity()) {
                return held;
            }
        }
        return closest(narrowed);
    }

    public static List<Target> narrowed(List<Target> targets, Target held, FollowSettings settings) {
        List<Target> wanted = new ArrayList<>(targets);
        if (held != null && settings.ignoreSameKind()) {
            wanted.removeIf(target -> target != held && target.sameKindAs(held));
        }
        if (settings.closestOnly()) {
            Target closest = closest(wanted);
            return closest == null ? wanted : List.of(closest);
        }
        return wanted;
    }

    public static Target closest(List<Target> targets) {
        Target best = null;
        for (Target target : targets) {
            if (best == null || target.distance() < best.distance()) {
                best = target;
            }
        }
        return best;
    }

    private static boolean eligible(Entity entity, LocalPlayer player, FollowSubject subject) {
        if (entity == null || !entity.isAlive() || entity == player) {
            return false;
        }
        return subject.matches(entity);
    }
}