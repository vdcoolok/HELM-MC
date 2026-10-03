package dev.helm.mine.drops;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;

import dev.helm.mine.target.TargetFilter;

public final class DropWatch {

    private DropWatch() {
    }

    public static List<Entity> matching(ClientLevel level, TargetFilter filter) {
        List<Entity> found = new ArrayList<>();
        for (Entity entity : level.entitiesForRendering()) {
            if (entity instanceof ItemEntity dropped
                    && DroppedTargets.wanted(dropped.getItem(), filter)) {
                found.add(dropped);
            }
        }
        return found;
    }

    public static boolean stillFalling(Entity entity) {
        return entity instanceof ItemEntity dropped
                && !dropped.onGround()
                && dropped.getDeltaMovement().y != 0.0D;
    }
}