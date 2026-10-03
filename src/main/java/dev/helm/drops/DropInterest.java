package dev.helm.drops;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.minecraft.world.item.Item;

public final class DropInterest {

    private final Map<Item, Long> learned = new HashMap<>();

    public void learn(Item item, long tick) {
        learned.put(item, tick);
    }

    public boolean wanted(Item item, long tick, long window) {
        Long at = learned.get(item);
        return at != null && tick - at < window;
    }

    public void expire(long tick, long window) {
        if (learned.isEmpty()) {
            return;
        }
        Iterator<Map.Entry<Item, Long>> entries = learned.entrySet().iterator();
        while (entries.hasNext()) {
            if (tick - entries.next().getValue() >= window) {
                entries.remove();
            }
        }
    }

    public int size() {
        return learned.size();
    }

    public void clear() {
        learned.clear();
    }
}
