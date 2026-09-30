package dev.helm.render;

import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;

public final class RouteRenderTypes {

    private static final RenderType WITH_DEPTH = RenderType.create("helm_lines_with_depth",
            setup(CompareOp.LESS_THAN_OR_EQUAL));

    private static final RenderType NO_DEPTH = RenderType.create("helm_lines_no_depth",
            setup(CompareOp.ALWAYS_PASS));

    private RouteRenderTypes() {
    }

    public static RenderType forPath(boolean ignoreDepth) {
        return ignoreDepth ? NO_DEPTH : WITH_DEPTH;
    }

    private static RenderSetup setup(CompareOp compare) {
        return RenderSetup.builder(RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
                .withLocation("pipelines/helm_lines")
                .withDepthStencilState(new DepthStencilState(compare, false))
                .build())
                .createRenderSetup();
    }
}
