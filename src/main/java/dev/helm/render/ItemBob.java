package dev.helm.render;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;

public final class ItemBob {

    private static final float BOB_SPEED = 10.0F;
    private static final float BOB_HEIGHT = 0.1F;
    private static final float BOB_BASE = 0.1F;
    private static final float HOVER_GAP = 0.0625F;

    private ItemBob() {
    }

    public static float rise(ItemEntity dropped, AABB model) {
        return Mth.sin(dropped.tickCount / BOB_SPEED + dropped.bobOffs) * BOB_HEIGHT
                + BOB_BASE - (float) model.minY + HOVER_GAP;
    }

    public static float spin(ItemEntity dropped) {
        return ItemEntity.getSpin(dropped.tickCount, dropped.bobOffs);
    }
}