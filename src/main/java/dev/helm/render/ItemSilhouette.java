package dev.helm.render;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.AABB;

public final class ItemSilhouette {

    private static ItemModelResolver resolver;

    private ItemSilhouette() {
    }

    public static List<double[]> corners(ItemEntity dropped) {
        ItemStackRenderState state = new ItemStackRenderState();
        resolver().updateForNonLiving(state, dropped.getItem(), ItemDisplayContext.GROUND, dropped);
        AABB box = state.getModelBoundingBox();
        List<double[]> corners = new ArrayList<>(8);
        corners.add(new double[]{box.minX, box.minY, box.minZ});
        corners.add(new double[]{box.maxX, box.minY, box.minZ});
        corners.add(new double[]{box.maxX, box.minY, box.maxZ});
        corners.add(new double[]{box.minX, box.minY, box.maxZ});
        corners.add(new double[]{box.minX, box.maxY, box.minZ});
        corners.add(new double[]{box.maxX, box.maxY, box.minZ});
        corners.add(new double[]{box.maxX, box.maxY, box.maxZ});
        corners.add(new double[]{box.minX, box.maxY, box.maxZ});
        return corners;
    }

    public static float modelFloor(List<double[]> corners) {
        float floor = Float.MAX_VALUE;
        for (double[] corner : corners) {
            floor = Math.min(floor, (float) corner[1]);
        }
        return floor == Float.MAX_VALUE ? 0.0F : floor;
    }

    private static ItemModelResolver resolver() {
        if (resolver == null) {
            resolver = new ItemModelResolver(Minecraft.getInstance().getModelManager());
        }
        return resolver;
    }
}