package dev.helm.movement.sprint;

public record SprintChoice(boolean sprint, int skipTo, boolean safeDescend, boolean releaseJump) {

    private static final int NO_SKIP = -1;
    private static final SprintChoice HOLD = new SprintChoice(false, NO_SKIP, false, false);
    private static final SprintChoice CHARGE = new SprintChoice(true, NO_SKIP, false, false);

    public static SprintChoice hold() {
        return HOLD;
    }

    public static SprintChoice charge() {
        return CHARGE;
    }

    public static SprintChoice holdSteadyDescend() {
        return new SprintChoice(false, NO_SKIP, true, false);
    }

    public static SprintChoice sprintReleasingJump() {
        return new SprintChoice(true, NO_SKIP, false, true);
    }

    public static SprintChoice sprint(int skipTo) {
        return new SprintChoice(true, skipTo, false, false);
    }

    public boolean skips() {
        return skipTo != NO_SKIP;
    }
}
