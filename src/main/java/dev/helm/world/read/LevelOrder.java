package dev.helm.world.read;

import java.util.Arrays;
import java.util.Comparator;

final class LevelOrder {

    private LevelOrder() {
    }

    static int[] nearestFirst(int sectionCount, int standingSection) {
        Integer[] sections = new Integer[sectionCount];
        for (int section = 0; section < sectionCount; section++) {
            sections[section] = section;
        }
        Arrays.sort(sections, Comparator.comparingInt(
                section -> Math.abs(section - standingSection)));
        int[] ordered = new int[sectionCount];
        for (int index = 0; index < sectionCount; index++) {
            ordered[index] = sections[index];
        }
        return ordered;
    }
}