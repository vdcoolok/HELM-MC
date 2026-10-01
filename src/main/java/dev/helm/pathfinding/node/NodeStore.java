package dev.helm.pathfinding.node;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import dev.helm.pathfinding.goal.Goal;

public final class NodeStore {

    private static final int INITIAL_CAPACITY = 1 << 14;

    private final Long2ObjectOpenHashMap<Node> nodes =
            new Long2ObjectOpenHashMap<>(INITIAL_CAPACITY);

    public Node at(int x, int y, int z, Goal goal) {
        long key = NodeKey.of(x, y, z);
        Node known = nodes.get(key);
        if (known != null) {
            return known;
        }
        Node node = new Node(x, y, z, goal);
        nodes.put(key, node);
        return node;
    }

    public int size() {
        return nodes.size();
    }

    public void clear() {
        nodes.clear();
    }
}
