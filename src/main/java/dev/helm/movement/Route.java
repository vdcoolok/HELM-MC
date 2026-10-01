package dev.helm.movement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dev.helm.movement.step.PlanStep;

public final class Route {

    private final List<PlanStep> steps;
    private final int[] destination;

    public Route(List<PlanStep> steps, int[] destination) {
        this.steps = List.copyOf(steps);
        this.destination = destination == null ? null : destination.clone();
    }

    public static Route of(List<PlanStep> steps, int[] destination) {
        return new Route(steps, destination);
    }

    public static Route empty() {
        return new Route(List.of(), null);
    }

    public List<PlanStep> steps() {
        return steps;
    }

    public int length() {
        return steps.size();
    }

    public PlanStep at(int index) {
        return steps.get(index);
    }

    public int[] destination() {
        return destination;
    }

    public Set<int[]> blocksToBreak() {
        Set<int[]> blocks = new HashSet<>();
        for (PlanStep step : steps) {
            blocks.addAll(step.blocksToBreak());
        }
        return blocks;
    }

    public int[][] positions() {
        int[][] blocks = new int[steps.size() + 1][];
        if (steps.isEmpty()) {
            return blocks;
        }
        PlanStep first = steps.get(0);
        blocks[0] = new int[]{first.fromX(), first.fromY(), first.fromZ()};
        for (int index = 0; index < steps.size(); index++) {
            PlanStep step = steps.get(index);
            blocks[index + 1] = new int[]{step.toX(), step.toY(), step.toZ()};
        }
        return blocks;
    }

    public int[] end() {
        if (steps.isEmpty()) {
            return destination;
        }
        PlanStep last = steps.get(steps.size() - 1);
        return new int[]{last.toX(), last.toY(), last.toZ()};
    }

    public boolean endsAt(int x, int y, int z) {
        int[] end = end();
        return end != null && end[0] == x && end[1] == y && end[2] == z;
    }

    public boolean holds(int x, int y, int z) {
        for (int[] block : positions()) {
            if (block[0] == x && block[1] == y && block[2] == z) {
                return true;
            }
        }
        return false;
    }

    public Set<int[]> blocksToPlace() {
        Set<int[]> blocks = new HashSet<>();
        for (PlanStep step : steps) {
            if (step.placeAt() != null) {
                blocks.add(step.placeAt());
            }
        }
        return blocks;
    }

    public int indexOfFeet(int x, int y, int z) {
        for (int index = 0; index < steps.size(); index++) {
            PlanStep step = steps.get(index);
            if (step.toX() == x && step.toY() == y && step.toZ() == z) {
                return index;
            }
        }
        return -1;
    }

    public Route withoutFirst(int count) {
        if (count <= 0) {
            return this;
        }
        if (count >= steps.size()) {
            return new Route(List.of(), destination);
        }
        List<PlanStep> kept = new ArrayList<>(steps.subList(count, steps.size()));
        kept.set(0, kept.get(0).startingFrom(steps.get(count - 1)));
        return new Route(kept, destination);
    }
}
