package dev.helm.world.read.section;

public final class LocalPaletteMatcher implements SectionMatcher {

    private final boolean[] accepted;

    public LocalPaletteMatcher(boolean[] accepted) {
        this.accepted = accepted;
    }

    @Override
    public boolean accepts(int index) {
        return accepted[index];
    }

    @Override
    public boolean fills() {
        return accepted.length == 1;
    }
}
