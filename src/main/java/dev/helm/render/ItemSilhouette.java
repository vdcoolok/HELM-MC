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

    private static final double PRECISION = 0.0005D;

    private static ItemModelResolver resolver;

    private ItemSilhouette() {
    }

    public static List<double[]> corners(ItemEntity dropped) {
        ItemStackRenderState state = new ItemStackRenderState();
        resolver().updateForNonLiving(state, dropped.getItem(), ItemDisplayContext.GROUND, dropped);
        List<double[]> found = new ArrayList<>();
        state.visitExtents((Vector3fc corner) -> {
            double x = corner.x();
            double y = corner.y();
            double z = corner.z();
            if (apart(found, x, y, z)) {
                found.add(new double[]{x, y, z});
            }
        });
        return found;
    }

    private static boolean apart(List<double[]> found, double x, double y, double z) {
        for (double[] corner : found) {
            if (Math.abs(corner[0] - x) < PRECISION
                    && Math.abs(corner[1] - y) < PRECISION
                    && Math.abs(corner[2] - z) < PRECISION) {
                return false;
            }
        }
        return true;
    }

    private static ItemModelResolver resolver() {
        if (resolver == null) {
            resolver = new ItemModelResolver(Minecraft.getInstance().getModelManager());
        }
        return resolver;
    }
}