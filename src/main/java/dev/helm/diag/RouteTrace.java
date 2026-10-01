package dev.helm.diag;

import java.util.List;

import dev.helm.movement.Route;
import dev.helm.movement.step.PlanStep;

public final class RouteTrace {

    private static final int PER_LINE = 4;

    private RouteTrace() {
    }

    public static void describe(Route route) {
        if (route == null) {
            Trace.instance().event("route", "route is null");
            return;
        }
        List<PlanStep> steps = route.steps();
        if (steps.isEmpty()) {
            Trace.instance().event("route", "no steps");
            return;
        }
        Trace.instance().event("route", "wanted " + steps.size() + " steps, "
                + ticks(steps) + " ticks, "
                + distinctDestinations(steps) + " distinct destinations, ends at "
                + destination(steps.get(steps.size() - 1)));
        Trace.instance().event("route", "standing at " + origin(steps.get(0))
                + ", must reach " + destination(steps.get(steps.size() - 1)));
        blocks(steps);
        list(steps);
        Trace.instance().barrier("route");
    }

    private static void blocks(List<PlanStep> steps) {
        for (int index = 0; index < steps.size(); index++) {
            PlanStep step = steps.get(index);
            if (step.blocksToBreak().isEmpty() && step.placeAt() == null) {
                continue;
            }
            Trace.instance().event("route", "[" + pad(index) + "] " + step.kind()
                    + " breaks " + describeAll(step.blocksToBreak())
                    + (step.placeAt() == null ? "" : " places " + describe(step.placeAt())));
        }
    }

    private static void list(List<PlanStep> steps) {
        StringBuilder line = new StringBuilder();
        int inLine = 0;
        for (int index = 0; index < steps.size(); index++) {
            if (inLine == 0) {
                line.append('[').append(pad(index)).append("] ");
            }
            line.append(step(steps.get(index)));
            inLine++;
            if (inLine == PER_LINE || index == steps.size() - 1) {
                Trace.instance().event("route", line.toString());
                line.setLength(0);
                inLine = 0;
            } else {
                line.append("  ");
            }
        }
    }

    private static String step(PlanStep step) {
        return step.kind() + " " + origin(step) + ">" + destination(step);
    }

    private static String origin(PlanStep step) {
        return step.fromX() + "," + step.fromY() + "," + step.fromZ();
    }

    private static String destination(PlanStep step) {
        return step.toX() + "," + step.toY() + "," + step.toZ();
    }

    private static String describe(int[] position) {
        return position[0] + "," + position[1] + "," + position[2];
    }

    private static String describeAll(List<int[]> positions) {
        StringBuilder out = new StringBuilder();
        for (int[] position : positions) {
            if (out.length() > 0) {
                out.append(' ');
            }
            out.append(describe(position));
        }
        return out.length() == 0 ? "nothing" : out.toString();
    }

    private static String pad(int index) {
        String text = Integer.toString(index);
        return index < 10 ? "0" + text : text;
    }

    private static int ticks(List<PlanStep> steps) {
        double total = 0;
        for (PlanStep step : steps) {
            total += step.cost();
        }
        return (int) Math.ceil(total);
    }

    private static int distinctDestinations(List<PlanStep> steps) {
        return (int) steps.stream().map(RouteTrace::destination).distinct().count();
    }
}
