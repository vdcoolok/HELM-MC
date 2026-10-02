package dev.helm.mixin.accessor;

import net.minecraft.client.renderer.item.ItemStackRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemStackRenderState.class)
public interface ItemRenderStateAccess {

    @Accessor("activeLayerCount")
    int activeLayers();

    @Accessor("layers")
    ItemStackRenderState.LayerRenderState[] layers();
}