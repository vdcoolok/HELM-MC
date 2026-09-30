package dev.helm.diag;

import dev.helm.movement.Route;
import dev.helm.movement.step.PlanStep;

public final class WalkTrace {

    private WalkTrace() {
    }

    public static void step(Route route, int index, int ticksOnStep, double recordedCost,
                            double liveCost) {
        PlanStep step = route.at(index);
        Trace.instance().repeat("walk", "walk", "step " + index + "/" + route.length()
                + " " + step.kind() + " -> " + step.toX() + "," + step.toY() + "," + step.toZ()
                + " ticks " + ticksOnStep
                + " cost " + Math.round(recordedCost) + "->" + Math.round(liveCost)
                + " break " + step.blocksToBreak().size()
                + " place " + (step.placeAt() == null ? "no" : "yes")
                + " aim " + step.move());
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

    private static String describe(int[] position) {
        return position[0] + "," + position[1] + "," + position[2];
    }
}
