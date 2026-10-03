package dev.helm.mine.drops;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import dev.helm.drops.DropCache;
import dev.helm.mine.target.TargetFilter;

public final class DroppedTargets {

    private DroppedTargets() {
    }

    public static List<BlockPos> gather(ClientLevel level, TargetFilter filter) {
        List<BlockPos> found = new ArrayList<>();
        for (Entity dropped : DropWatch.matching(level, filter)) {
            found.add(dropped.blockPosition());
        }
        return found;
    }

    public static boolean wanted(ItemStack stack, TargetFilter filter) {
        if (stack.isEmpty()) {
            return false;
        }
        Item item = stack.getItem();
        if (DropCache.instance().wanted(item)) {
            return true;
        }
        return filter.items().contains(item);
    }
}