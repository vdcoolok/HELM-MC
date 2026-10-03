package dev.helm.world.read;

final class SectionOrder {

    private final int count;
    private int up;
    private int down;
    private boolean upFirst = true;

    SectionOrder(int sectionCount, int standingSection) {
        this.count = sectionCount;
        int standing = Math.min(Math.max(standingSection, 0), Math.max(sectionCount - 1, 0));
        this.up = standing;
        this.down = standing - 1;
    }

    boolean hasNext() {
        return down >= 0 || up < count;
    }

    int next() {
        boolean goingUp = upFirst;
        upFirst = !upFirst;
        if (goingUp) {
            return up < count ? up++ : down--;
        }
        return down >= 0 ? down-- : up++;
    }
}
