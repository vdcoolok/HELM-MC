package dev.helm.render;

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

    public static AABB modelBox(ItemEntity dropped) {
        ItemStackRenderState state = new ItemStackRenderState();
        resolver().updateForNonLiving(state, dropped.getItem(), ItemDisplayContext.GROUND, dropped);
        return state.getModelBoundingBox();
    }

    public static float floorOf(AABB box) {
        return (float) box.minY;
    }

    private static ItemModelResolver resolver() {
        if (resolver == null) {
            resolver = new ItemModelResolver(Minecraft.getInstance().getModelManager());
        }
        return resolver;
    }
}