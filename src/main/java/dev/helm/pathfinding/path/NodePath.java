package dev.helm.pathfinding.path;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.helm.pathfinding.move.MoveKind;
import dev.helm.pathfinding.node.Node;

public final class NodePath {

    private final List<Leg> legs;

    private NodePath(List<Leg> legs) {
        this.legs = legs;
    }

    public static NodePath of(Node from, Node to) {
        if (from == null || to == null) {
            return new NodePath(List.of());
        }
        List<Leg> legs = new ArrayList<>();
        for (Node node = to; node != null && node.previous != null; node = node.previous) {
            legs.add(new Leg(node.previous.x, node.previous.y, node.previous.z,
                    node.x, node.y, node.z, node.arrivedBy));
        }
        Collections.reverse(legs);
        return new NodePath(List.copyOf(legs));
    }

    public static NodePath ofNodes(List<Leg> legs) {
        return new NodePath(List.copyOf(legs));
    }

    public record Leg(int fromX, int fromY, int fromZ, int toX, int toY, int toZ, MoveKind by) {
    }

    public List<Leg> legs() {
        return legs;
    }

    public NodePath truncatedTo(int movements) {
        if (movements <= 0) {
            return new NodePath(List.of());
        }
        if (movements >= legs.size()) {
            return this;
        }
        return new NodePath(legs.subList(0, movements));
    }

    public int length() {
        return legs.size();
    }

    public boolean isEmpty() {
        return legs.isEmpty();
    }

    public int[] end() {
        if (legs.isEmpty()) {
            return null;
        }
        Leg last = legs.get(legs.size() - 1);
        return new int[]{last.toX(), last.toY(), last.toZ()};
    }

    public int[] start() {
        if (legs.isEmpty()) {
            return null;
        }
        Leg first = legs.get(0);
        return new int[]{first.fromX(), first.fromY(), first.fromZ()};
    }

    public List<int[]> positions() {
        List<int[]> all = new ArrayList<>(legs.size() + 1);
        if (legs.isEmpty()) {
            return all;
        }
        Leg first = legs.get(0);
        all.add(new int[]{first.fromX(), first.fromY(), first.fromZ()});
        for (Leg leg : legs) {
            all.add(new int[]{leg.toX(), leg.toY(), leg.toZ()});
        }
        return all;
    }
}
