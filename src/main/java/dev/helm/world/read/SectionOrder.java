package dev.helm.world.read;

final class SectionOrder {

    private final int[] indices;

    SectionOrder(int sectionCount, int standingSection) {
        this.indices = around(sectionCount, standingSection);
    }

    int length() {
        return indices.length;
    }

    int at(int index) {
        return indices[index];
    }

    private static int[] around(int sectionCount, int standingSection) {
        int count = Math.max(sectionCount, 0);
        int standing = Math.min(Math.max(standingSection, 0), Math.max(count - 1, 0));
        int[] order = new int[count];
        int up = standing;
        int down = standing - 1;
        boolean upFirst = true;
        for (int at = 0; at < count; at++) {
            boolean goingUp = upFirst;
            upFirst = !upFirst;
            if (goingUp) {
                order[at] = up < count ? up++ : down--;
            } else {
                order[at] = down >= 0 ? down-- : up++;
            }
        }
        return order;
    }
}
