package dev.helm.diag;

import java.util.List;
import dev.helm.movement.Route;
import dev.helm.movement.step.PlanStep;

public final class RouteTrace {

    private static final int HEAD_STEPS = 12;
    private static final int TAIL_STEPS = 6;

    private RouteTrace() {
    }

    public static void describe(Route route) {
        if (route == null) {
            Trace.instance().event("route", "route is null");
            return;
        }
        List<PlanStep> steps = route.steps();
        Trace.instance().event("route", steps.size() + " steps, "
                + distinctPositions(steps) + " distinct destinations");
        if (steps.isEmpty()) {
            return;
        }
        Trace.instance().event("route", "from " + at(steps, 0));
        Trace.instance().event("route", "to   " + at(steps, steps.size() - 1));
        head(steps);
        tail(steps);
    }

    private static void head(List<PlanStep> steps) {
        int shown = Math.min(HEAD_STEPS, steps.size());
        StringBuilder line = new StringBuilder("first " + shown + ":");
        for (int index = 0; index < shown; index++) {
            line.append(' ').append(describe(steps.get(index)));
        }
        Trace.instance().event("route", line.toString());
    }

    private static void tail(List<PlanStep> steps) {
        if (steps.size() <= HEAD_STEPS) {
            return;
        }
        int shown = Math.min(TAIL_STEPS, steps.size());
        StringBuilder line = new StringBuilder("last " + shown + ":");
        for (int index = steps.size() - shown; index < steps.size(); index++) {
            line.append(' ').append(describe(steps.get(index)));
        }
        Trace.instance().event("route", line.toString());
        int hidden = steps.size() - HEAD_STEPS - shown;
        if (hidden > 0) {
            Trace.instance().event("route", "..." + hidden + " steps in between...");
        }
    }

    private static String describe(PlanStep step) {
        return at(step) + " " + step.kind() + "/" + step.move();
    }

    private static String at(List<PlanStep> steps, int index) {
        PlanStep step = steps.get(index);
        return step.fromX() + "," + step.fromY() + "," + step.fromZ();
    }

    private static String at(PlanStep step) {
        return step.toX() + "," + step.toY() + "," + step.toZ();
    }

    private static int distinctPositions(List<PlanStep> steps) {
        return (int) steps.stream().map(RouteTrace::at).distinct().count();
    }
}
