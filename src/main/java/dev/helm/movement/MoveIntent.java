package dev.helm.movement;

import dev.helm.aim.Aim;

public final class MoveIntent {

    private Aim aim;
    private boolean forceAim;

    public MoveIntent() {
    }

    public MoveIntent(Aim aim, boolean forceAim) {
        this.aim = aim;
        this.forceAim = forceAim;
    }

    public MoveIntent aimedAt(Aim wanted, boolean force) {
        this.aim = wanted;
        this.forceAim = force;
        return this;
    }

    public MoveIntent keepPitch(Aim wanted) {
        this.aim = wanted;
        this.forceAim = true;
        return this;
    }

    public boolean hasAim() {
        return aim != null;
    }

    public Aim aim() {
        if (aim == null) {
            throw new IllegalStateException("No aim was requested for this move");
        }
        return aim;
    }

    public boolean forcesAim() {
        return forceAim;
    }

    public void clear() {
        aim = null;
        forceAim = false;
    }
}
