package dev.helm.mixin.accessor;

import java.util.function.Supplier;

import net.minecraft.client.renderer.item.ItemStackRenderState;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemStackRenderState.LayerRenderState.class)
public interface ItemLayerAccess {

    @Accessor("extents")
    Supplier<Vector3fc[]> extents();
}