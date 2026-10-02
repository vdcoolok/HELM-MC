package dev.helm.render;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Vector3fc;

public final class ItemSilhouette {

    private static ItemModelResolver resolver;

    private ItemSilhouette() {
    }

    public static List<double[]> corners(ItemEntity dropped) {
        ItemStackRenderState state = new ItemStackRenderState();
        resolver().updateForNonLiving(state, dropped.getItem(), ItemDisplayContext.GROUND, dropped);
        List<double[]> points = new ArrayList<>();
        state.visitExtents((Vector3fc corner) -> points.add(
                new double[]{corner.x(), corner.y(), corner.z()}));
        return points;
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