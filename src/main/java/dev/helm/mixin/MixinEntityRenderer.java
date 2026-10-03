package dev.helm.mixin;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.helm.outline.ItemSilhouette;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer {

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void helmOutlineWantedItems(Entity entity, EntityRenderState state,
                                         float partialTicks, CallbackInfo callback) {
        if (state.outlineColor != 0) {
            return;
        }
        int colour = ItemSilhouette.colourOf(entity);
        if (colour != 0) {
            state.outlineColor = colour;
        }
    }
}
