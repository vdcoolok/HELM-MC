package dev.helm.mixin.accessor;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.renderer.RenderPipelines;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(RenderPipelines.class)
public class RenderPipelineAccess {

    @Final
    @Shadow
    private static RenderPipeline.Snippet LINES_SNIPPET;

    @Shadow
    private static RenderPipeline register(final RenderPipeline renderPipeline) {
        throw new AssertionError();
    }

    public static RenderPipeline.Snippet helmLinesSnippet() {
        return LINES_SNIPPET;
    }

    public static RenderPipeline helmRegisterPipeline(RenderPipeline pipeline) {
        return register(pipeline);
    }
}
