package dev.helm.pathfinding.node;

import dev.helm.pathfinding.goal.Goal;

public final class NodeStore {

    private Node[] slots = new Node[1024];
    private long[] keys = new long[1024];
    private int size;

    public Node at(int x, int y, int z, Goal goal) {
        long key = NodeKey.of(x, y, z);
        int bucket = bucketOf(key);
        while (slots[bucket] != null) {
            if (keys[bucket] == key) {
                return slots[bucket];
            }
            bucket = (bucket + 1) & mask();
        }
        Node node = new Node(x, y, z, goal);
        slots[bucket] = node;
        keys[bucket] = key;
        size++;
        if (size * 2 >= slots.length) {
            grow();
        }
        return node;
    }

    public int size() {
        return size;
    }

    public void clear() {
        java.util.Arrays.fill(slots, null);
        size = 0;
    }

    private void grow() {
        Node[] oldSlots = slots;
        long[] oldKeys = keys;
        slots = new Node[oldSlots.length << 1];
        keys = new long[oldKeys.length << 1];
        size = 0;
        for (int index = 0; index < oldSlots.length; index++) {
            if (oldSlots[index] == null) {
                continue;
            }
            int bucket = bucketOf(oldKeys[index]);
            while (slots[bucket] != null) {
                bucket = (bucket + 1) & mask();
            }
            slots[bucket] = oldSlots[index];
            keys[bucket] = oldKeys[index];
            size++;
        }
    }

    private int bucketOf(long key) {
        long mixed = key * 0x9E3779B97F4A7C15L;
        return (int) ((mixed ^ (mixed >>> 32)) & mask());
    }

    private int mask() {
        return slots.length - 1;
    }
}