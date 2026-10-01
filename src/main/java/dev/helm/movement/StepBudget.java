package dev.helm.movement;

import dev.helm.movement.step.PlanStep;
import dev.helm.pathfinding.cost.MoveCosts;
import dev.helm.setting.MovementSettings;

public final class StepBudget {

    private final StepPricer pricer;
    private final MovementSettings settings;
    private final int index;
    private final PlanStep step;

    private final double original;
    private double live;
    private boolean stale = true;

    public StepBudget(StepPricer pricer, MovementSettings settings, int index, PlanStep step) {
        this.pricer = pricer;
        this.settings = settings;
        this.index = index;
        this.step = step;
        this.original = step.cost();
    }

    public void refresh() {
        stale = true;
    }

    public double original() {
        return original;
    }

    public double live() {
        if (stale) {
            live = pricer.reprice(step);
            stale = false;
        }
        return live;
    }

    public boolean impossible() {
        return live() >= MoveCosts.IMPOSSIBLE;
    }

    public boolean roseTooFar() {
        return live() - original > settings.maxCostIncrease();
    }

    public boolean anyAheadImpossible(Route route) {
        if (!step.plannedWhileLoaded()) {
            return false;
        }
        int lookahead = Math.min(settings.costVerificationLookahead(), route.length() - index - 1);
        for (int ahead = 1; ahead < lookahead; ahead++) {
            if (pricer.impossible(route.at(index + ahead))) {
                return true;
            }
        }
        return false;
    }
}
