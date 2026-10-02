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

    private static final double TOLERANCE = 1.25D;
    private static final double COINCIDENT = 1.0E-6D;
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
        double span = shortestGap(points);
        if (span <= 0.0D) {
            return List.of();
        }
        double limit = span * span * TOLERANCE * TOLERANCE;
        List<int[]> edges = new ArrayList<>();
        for (int from = 0; from < points.size(); from++) {
            for (int to = from + 1; to < points.size(); to++) {
                if (squared(points.get(from), points.get(to)) <= limit) {
                    edges.add(new int[]{from, to});
                }
            }
        }
        return edges;
    }

    private static double shortestGap(List<double[]> points) {
        double shortest = Double.MAX_VALUE;
        for (int from = 0; from < points.size(); from++) {
            for (int to = from + 1; to < points.size(); to++) {
                double gap = squared(points.get(from), points.get(to));
                if (gap > COINCIDENT && gap < shortest) {
                    shortest = gap;
                }
            }
        }
        return shortest == Double.MAX_VALUE ? 0.0D : Math.sqrt(shortest);
    }

    private static double squared(double[] from, double[] to) {
        double dx = from[0] - to[0];
        double dy = from[1] - to[1];
        double dz = from[2] - to[2];
        return dx * dx + dy * dy + dz * dz;
    }

    private static ItemModelResolver resolver() {
        if (resolver == null) {
            resolver = new ItemModelResolver(Minecraft.getInstance().getModelManager());
        }
        return resolver;
    }
}