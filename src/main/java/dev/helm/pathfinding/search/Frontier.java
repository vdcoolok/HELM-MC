package dev.helm.pathfinding.search;

import dev.helm.pathfinding.node.Node;

public final class Frontier {

    private Node[] heap = new Node[1024];
    private int count;

    public boolean isEmpty() {
        return count == 0;
    }

    public int size() {
        return count;
    }

    public void add(Node node) {
        if (count + 1 >= heap.length) {
            grow();
        }
        count++;
        heap[count] = node;
        node.queuedAt = count;
        siftUp(node);
    }

    public void refresh(Node node) {
        siftUp(node);
    }

    public Node takeLowest() {
        if (count == 0) {
            throw new IllegalStateException("Frontier is empty");
        }
        Node lowest = heap[1];
        Node last = heap[count];
        heap[count] = null;
        count--;
        lowest.queuedAt = -1;
        if (count > 0) {
            heap[1] = last;
            last.queuedAt = 1;
            siftDown(last);
        }
        return lowest;
    }

    private void siftUp(Node node) {
        int index = node.queuedAt;
        while (index > 1) {
            int parentIndex = index >>> 1;
            Node parent = heap[parentIndex];
            if (parent.combined <= node.combined) {
                break;
            }
            heap[index] = parent;
            parent.queuedAt = index;
            index = parentIndex;
        }
        heap[index] = node;
        node.queuedAt = index;
    }

    private void siftDown(Node node) {
        int index = node.queuedAt;
        int half = count >>> 1;
        while (index <= half) {
            int childIndex = index << 1;
            Node child = heap[childIndex];
            int rightIndex = childIndex + 1;
            if (rightIndex <= count && heap[rightIndex].combined < child.combined) {
                childIndex = rightIndex;
                child = heap[rightIndex];
            }
            if (child.combined >= node.combined) {
                break;
            }
            heap[index] = child;
            child.queuedAt = index;
            index = childIndex;
        }
        heap[index] = node;
        node.queuedAt = index;
    }

    private void grow() {
        Node[] larger = new Node[heap.length << 1];
        System.arraycopy(heap, 0, larger, 0, heap.length);
        heap = larger;
    }
}