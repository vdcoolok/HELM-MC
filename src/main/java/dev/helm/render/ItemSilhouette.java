package dev.helm.render;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3fc;

public final class ItemSilhouette {

    private static final double EDGE = 0.005D;
    private static ItemModelResolver resolver;

    private ItemSilhouette() {
    }

    public static List<double[]> points(Entity dropped, ItemStack stack) {
        ItemStackRenderState state = new ItemStackRenderState();
        resolver().updateForNonLiving(state, stack, ItemDisplayContext.GROUND, dropped);
        List<double[]> points = new ArrayList<>();
        state.visitExtents((Vector3fc corner) -> points.add(
                new double[]{corner.x(), corner.y(), corner.z()}));
        return points;
    }

    public static List<int[]> edges(List<double[]> points) {
        List<int[]> edges = new ArrayList<>();
        for (int from = 0; from < points.size(); from++) {
            for (int to = from + 1; to < points.size(); to++) {
                if (joined(points.get(from), points.get(to))) {
                    edges.add(new int[]{from, to});
                }
            }
        }
        return edges;
    }

    private static boolean joined(double[] from, double[] to) {
        double dx = from[0] - to[0];
        double dy = from[1] - to[1];
        double dz = from[2] - to[2];
        return dx * dx + dy * dy + dz * dz <= EDGE * EDGE;
    }

    private static ItemModelResolver resolver() {
        if (resolver == null) {
            resolver = new ItemModelResolver(Minecraft.getInstance().getModelManager());
        }
        return resolver;
    }
}