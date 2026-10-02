package dev.helm.render;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;

public final class BlockSilhouette {

    private static BlockModelResolver resolver;

    private BlockSilhouette() {
    }

    public static List<double[]> quads(BlockState state) {
        BlockModelRenderState model = new BlockModelRenderState();
        resolver().update(model, state, BlockDisplayContext.create());
        List<double[]> corners = new ArrayList<>();
        for (BlockStateModelPart part : model.setupModel(new Matrix4f(), false)) {
            for (Direction face : Direction.values()) {
                for (BakedQuad quad : part.getQuads(face)) {
                    for (int i = 0; i < BakedQuad.VERTEX_COUNT; i++) {
                        corners.add(point(quad, i));
                    }
                }
            }
        }
        return corners;
    }

    public static LineBatch outline(LineBatch batch, List<double[]> corners) {
        for (int i = 0; i + BakedQuad.VERTEX_COUNT <= corners.size(); i += BakedQuad.VERTEX_COUNT) {
            for (int edge = 0; edge < BakedQuad.VERTEX_COUNT; edge++) {
                double[] from = corners.get(i + edge);
                double[] to = corners.get(i + (edge + 1) % BakedQuad.VERTEX_COUNT);
                batch.segment(from[0], from[1], from[2], to[0], to[1], to[2]);
            }
        }
        return batch;
    }

    private static double[] point(BakedQuad quad, int index) {
        var corner = quad.position(index);
        return new double[]{corner.x(), corner.y(), corner.z()};
    }

    private static BlockModelResolver resolver() {
        if (resolver == null) {
            resolver = new BlockModelResolver(Minecraft.getInstance().getModelManager());
        }
        return resolver;
    }
}