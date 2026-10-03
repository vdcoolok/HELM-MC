package dev.helm.drops;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

public final class BreakLedger {

    private static final int LIMIT = 256;

    private final Map<BlockPos, BrokenBlock> broken = new LinkedHashMap<>();
    private AABB reach;

    public void remember(BlockPos pos, long tick) {
        BlockPos key = pos.immutable();
        BrokenBlock existing = broken.get(key);
        if (existing != null && existing.tick() == tick) {
            return;
        }
        if (existing == null && broken.size() >= LIMIT) {
            Iterator<BrokenBlock> oldest = broken.values().iterator();
            if (oldest.hasNext()) {
                oldest.next();
                oldest.remove();
            }
        }
        broken.put(key, new BrokenBlock(key, tick));
        reach = null;
    }

    public void expire(long tick, long window) {
        if (broken.isEmpty()) {
            return;
        }
        Iterator<BrokenBlock> entries = broken.values().iterator();
        boolean dropped = false;
        while (entries.hasNext()) {
            if (entries.next().olderThan(tick, window)) {
                entries.remove();
                dropped = true;
            }
        }
        if (dropped) {
            reach = null;
        }
    }

    public boolean empty() {
        return broken.isEmpty();
    }

    public boolean holds(double x, double y, double z, double radius) {
        if (broken.isEmpty() || !reach().contains(x, y, z)) {
            return false;
        }
        for (BrokenBlock site : broken.values()) {
            if (site.near(x, y, z, radius)) {
                return true;
            }
        }
        return false;
    }

    public void clear() {
        broken.clear();
        reach = null;
    }

    private AABB reach() {
        AABB current = reach;
        if (current != null) {
            return current;
        }
        if (broken.isEmpty()) {
            current = new AABB(0, 0, 0, 0, 0, 0);
        } else {
            BlockPos first = broken.keySet().iterator().next();
            double minX = first.getX();
            double minY = first.getY();
            double minZ = first.getZ();
            double maxX = minX;
            double maxY = minY;
            double maxZ = minZ;
            for (BlockPos pos : broken.keySet()) {
                minX = Math.min(minX, pos.getX());
                minY = Math.min(minY, pos.getY());
                minZ = Math.min(minZ, pos.getZ());
                maxX = Math.max(maxX, pos.getX());
                maxY = Math.max(maxY, pos.getY());
                maxZ = Math.max(maxZ, pos.getZ());
            }
            current = new AABB(minX, minY, minZ, maxX + 1, maxY + 1, maxZ + 1);
        }
        reach = current;
        return current;
    }
}
