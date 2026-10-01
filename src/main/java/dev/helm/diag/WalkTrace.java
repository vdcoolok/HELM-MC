package dev.helm.diag;

import java.util.Map;

import dev.helm.control.Control;
import dev.helm.movement.MoveTick;
import dev.helm.movement.Route;
import dev.helm.movement.step.PlanStep;
import dev.helm.movement.step.StepContext;

public final class WalkTrace {

    private WalkTrace() {
    }

    public static void step(Route route, int index, int ticksOnStep, double recordedCost,
                            double liveCost, StepContext context, MoveTick tick,
                            double offRoute, double toTarget) {
        PlanStep step = route.at(index);
        var player = context.player();
        StringBuilder out = new StringBuilder();
        out.append("step ").append(index).append('/').append(route.length())
                .append(' ').append(step.kind())
                .append(" want ").append(step.fromX()).append(',').append(step.fromY())
                .append(',').append(step.fromZ())
                .append(" -> ").append(step.toX()).append(',').append(step.toY())
                .append(',').append(step.toZ())
                .append(" ticks ").append(ticksOnStep)
                .append(" cost ").append(Math.round(recordedCost))
                .append("->").append(Math.round(liveCost))
                .append(" break ").append(step.blocksToBreak().size())
                .append(" place ").append(step.placeAt() == null ? "no" : "yes")
                .append(" aim ").append(step.move());
        if (player != null) {
            out.append(" am ").append(PlayerReport.at(player))
                    .append(" at ").append(PlayerReport.feet(player))
                    .append(' ').append(PlayerReport.footing(player))
                    .append(' ').append(PlayerReport.motion(player))
                    .append(' ').append(PlayerReport.facing(player))
                    .append(" sprint ").append(PlayerReport.sprinting(player))
                    .append(' ').append(PlayerReport.food(player))
                    .append(" toTarget ").append(round(toTarget))
                    .append(" offRoute ").append(round(offRoute));
        }
        out.append(" keys ").append(keys(tick));
        Trace.instance().event("walk", out.toString());
    }

    public static void rewind(Route route, int from, int to, int[] feet) {
        Trace.instance().event("walk", "rewound from step " + from + " to " + to
                + " because the player is standing on " + describe(feet));
    }

    public static void skip(Route route, int from, int to, int[] feet) {
        Trace.instance().event("walk", "skipped from step " + from + " to " + to
                + " because the player is standing on " + describe(feet));
    }

    public static void paused(Route route, int index, int[] unloaded) {
        Trace.instance().pulse("walk-paused", "walk", "paused at step " + index
                + "/" + route.length() + " waiting for chunk "
                + (unloaded[0] >> 4) + "," + (unloaded[2] >> 4));
    }

    public static void abandon(int index, int total, PlanStep step, String reason, int[] feet) {
        StringBuilder where = new StringBuilder(reason);
        where.append(" at step ").append(index).append('/').append(total);
        if (step != null) {
            where.append(' ').append(step.kind())
                    .append(" want ").append(step.toX()).append(',').append(step.toY())
                    .append(',').append(step.toZ())
                    .append(" from ").append(step.fromX()).append(',').append(step.fromY())
                    .append(',').append(step.fromZ());
        }
        where.append(" player on ").append(describe(feet));
        Trace.instance().event("walk", where.toString());
    }
    public static void finished(Route route, int index, int[] feet) {
        Trace.instance().event("walk", "route finished after " + index + " of "
                + route.length() + " steps, player on " + describe(feet));
    }

    public static void sprinting(boolean sprinting) {
        Trace.instance().repeat("sprint", "walk", "sprinting " + sprinting);
    }

    private static String keys(MoveTick tick) {
        Map<Control, Boolean> pressed = tick.inputs().view();
        StringBuilder out = new StringBuilder();
        for (Map.Entry<Control, Boolean> entry : pressed.entrySet()) {
            if (!Boolean.TRUE.equals(entry.getValue())) {
                continue;
            }
            if (out.length() > 0) {
                out.append('+');
            }
            out.append(shortName(entry.getKey()));
        }
        return out.length() == 0 ? "none" : out.toString();
    }

    private static String shortName(Control control) {
        return switch (control) {
            case MOVE_FORWARD -> "fwd";
            case MOVE_BACK -> "back";
            case MOVE_LEFT -> "left";
            case MOVE_RIGHT -> "right";
            case JUMP -> "jump";
            case SNEAK -> "sneak";
            case SPRINT -> "sprint";
            case ATTACK -> "break";
            case USE -> "place";
        };
    }

    private static String round(double value) {
        return String.format("%.2f", value);
    }

    private static String describe(int[] position) {
        return position[0] + "," + position[1] + "," + position[2];
    }
}
