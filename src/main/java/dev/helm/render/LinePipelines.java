package dev.helm.render;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.platform.BlendFactor;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import net.minecraft.client.renderer.StagedVertexBuffer;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;

import dev.helm.mixin.accessor.RenderPipelineAccess;
import dev.helm.mixin.accessor.RenderTypeAccess;

public final class LinePipelines {

    private static final RenderPipeline.Snippet LINES = RenderPipeline
            .builder(RenderPipelineAccess.helmLinesSnippet())
            .withColorTargetState(new ColorTargetState(new BlendFunction(
                    BlendFactor.SRC_ALPHA,
                    BlendFactor.ONE_MINUS_SRC_ALPHA,
                    BlendFactor.ONE,
                    BlendFactor.ZERO)))
            .withDepthStencilState(
                    new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false))
            .withCull(false)
            .buildSnippet();

    private static final RenderType WITH_DEPTH = RenderTypeAccess.helmCreate(
            "renderType/helm_lines_with_depth",
            RenderSetup.builder(RenderPipeline.builder(LINES)
                    .withLocation("pipelines/helm_lines_with_depth")
                    .withDepthStencilState(
                            new DepthStencilState(CompareOp.LESS_THAN_OR_EQUAL, false))
                    .build())
                    .createRenderSetup());

    private static final RenderType NO_DEPTH = RenderTypeAccess.helmCreate(
            "renderType/helm_lines_no_depth",
            RenderSetup.builder(RenderPipeline.builder(LINES)
                    .withLocation("pipelines/helm_lines_no_depth")
                    .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
                    .build())
                    .createRenderSetup());

    private LinePipelines() {
    }

    public static StagedVertexBuffer.Draw begin(StagedVertexBuffer buffer) {
        return buffer.appendDraw(
                com.mojang.blaze3d.vertex.DefaultVertexFormat.POSITION_COLOR_NORMAL_LINE_WIDTH,
                PrimitiveTopology.LINES);
    }

    public static RenderType forDepth(boolean ignoreDepth) {
        return ignoreDepth ? NO_DEPTH : WITH_DEPTH;
    }
}
