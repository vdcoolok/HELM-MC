package dev.helm.render;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import dev.helm.mixin.accessor.ItemLayerAccess;
import dev.helm.mixin.accessor.ItemRenderStateAccess;

public final class ItemSilhouette {

    private static ItemModelResolver resolver;

    private ItemSilhouette() {
    }

    public static List<double[]> outline(ItemEntity dropped) {
        ItemStackRenderState state = built(dropped);
        ItemRenderStateAccess access = (ItemRenderStateAccess) state;
        if (access.activeLayers() < 1 || access.layers().length < 1) {
            return List.of();
        }
        ItemStackRenderState.LayerRenderState layer = access.layers()[0];
        Matrix4fc transform = ((ItemLayerAccess) layer).localTransform();
        List<double[]> corners = new ArrayList<>();
        for (BakedQuad quad : layer.prepareQuadList()) {
            for (int i = 0; i < BakedQuad.VERTEX_COUNT; i++) {
                Vector3fc corner = quad.position(i);
                Vector3f at = new Vector3f(corner).mulPosition(transform);
                corners.add(new double[]{at.x(), at.y(), at.z()});
            }
        }
        return corners;
    }

    public static AABB modelBox(ItemEntity dropped) {
        return built(dropped).getModelBoundingBox();
    }

    private static ItemStackRenderState built(ItemEntity dropped) {
        ItemStackRenderState state = new ItemStackRenderState();
        resolver().updateForNonLiving(state, dropped.getItem(), ItemDisplayContext.GROUND, dropped);
        return state;
    }

    private static ItemModelResolver resolver() {
        if (resolver == null) {
            resolver = new ItemModelResolver(Minecraft.getInstance().getModelManager());
        }
        return resolver;
    }
}