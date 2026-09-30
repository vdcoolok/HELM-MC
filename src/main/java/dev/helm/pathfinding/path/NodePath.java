package dev.helm.pathfinding.path;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.helm.pathfinding.node.Node;

public final class NodePath {

    private final List<int[]> steps;

    private NodePath(List<int[]> steps) {
        this.steps = steps;
    }

    public static NodePath of(Node from, Node to) {
        if (from == null || to == null) {
            return new NodePath(List.of());
        }
        List<int[]> steps = new ArrayList<>();
        for (Node node = to; node != null; node = node.previous) {
            steps.add(new int[]{node.x, node.y, node.z});
            if (node == from) {
                break;
            }
        }
        Collections.reverse(steps);
        return new NodePath(List.copyOf(steps));
    }

    public List<int[]> steps() {
        return steps;
    }

    public int length() {
        return steps.size();
    }

    public boolean isEmpty() {
        return steps.isEmpty();
    }

    public int[] last() {
        return steps.isEmpty() ? null : steps.get(steps.size() - 1);
    }

    public int[] first() {
        return steps.isEmpty() ? null : steps.get(0);
    }
}