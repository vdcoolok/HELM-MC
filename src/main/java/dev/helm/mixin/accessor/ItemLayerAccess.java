package dev.helm.mixin.accessor;

import net.minecraft.client.renderer.item.ItemStackRenderState;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemStackRenderState.LayerRenderState.class)
public interface ItemLayerAccess {

    @Accessor("localTransform")
    Matrix4f localTransform();
}