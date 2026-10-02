package dev.helm.mixin;

import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.joml.Matrix4fc;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.helm.render.RouteOverlay;

@Mixin(LevelRenderer.class)
public abstract class MixinLevelRenderer {

    @Inject(method = "render", at = @At("RETURN"))
    private void helmDrawRoute(GraphicsResourceAllocator allocator,
                               DeltaTracker deltaTracker,
                               boolean renderBlockOutline,
                               CameraRenderState camera,
                               Matrix4fc modelView,
                               GpuBufferSlice fog,
                               Vector4f fogColour,
                               boolean renderSky,
                               CallbackInfo callback) {
        PoseStack pose = new PoseStack();
        pose.mulPose(modelView);
        RouteOverlay.draw(pose, camera);
    }
}
