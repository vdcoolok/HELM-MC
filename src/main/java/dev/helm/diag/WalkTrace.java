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
                            double liveCost, StepContext context, MoveTick tick) {
        PlanStep step = route.at(index);
        Trace.instance().repeat("walk", "walk", "step " + index + "/" + route.length()
                + " " + step.kind() + " -> " + step.toX() + "," + step.toY() + "," + step.toZ()
                + " ticks " + ticksOnStep
                + " cost " + Math.round(recordedCost) + "->" + Math.round(liveCost)
                + " break " + step.blocksToBreak().size()
                + " place " + (step.placeAt() == null ? "no" : "yes")
                + " aim " + step.move()
                + " at " + body(context)
                + " keys " + keys(tick));
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

    public static void abandon(Route route, int index, String reason, int[] feet) {
        StringBuilder where = new StringBuilder(reason);
        where.append(" at step ").append(index).append('/').append(route.length());
        if (index >= 0 && index < route.length()) {
            PlanStep step = route.at(index);
            where.append(" ").append(step.kind())
                    .append(" -> ").append(step.toX()).append(',').append(step.toY())
                    .append(',').append(step.toZ());
        }
        where.append(" player on ").append(describe(feet));
        Trace.instance().event("walk", where.toString());
    }

    public static void finished(Route route, int index) {
        Trace.instance().event("walk", "route finished after " + index + " of "
                + route.length() + " steps");
    }

    public static void sprinting(boolean sprinting) {
        Trace.instance().repeat("sprint", "walk", "sprinting " + sprinting);
    }

    private static String body(StepContext context) {
        var player = context.player();
        if (player == null) {
            return "none";
        }
        return String.format("%.2f/%.2f/%.2f yaw %.1f pitch %.1f %s",
                player.getX(), player.getY(), player.getZ(),
                player.getYRot(), player.getXRot(),
                player.onGround() ? "ground" : "air");
    }

    private static String keys(MoveTick tick) {
        Map<Control, Boolean> pressed = tick.inputs().view();
        if (pressed.isEmpty()) {
            return "-";
        }
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
        return out.length() == 0 ? "-" : out.toString();
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

    private static String describe(int[] position) {
        return position[0] + "," + position[1] + "," + position[2];
    }
}
