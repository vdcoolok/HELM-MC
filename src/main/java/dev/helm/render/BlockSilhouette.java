package dev.helm.render;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Matrix4fc;
import org.joml.Vector3f;

import dev.helm.mixin.accessor.BlockModelAccess;

public final class BlockSilhouette {

    private static BlockModelResolver resolver;

    private BlockSilhouette() {
    }

    public static Mesh mesh(BlockState state) {
        BlockModelRenderState model = new BlockModelRenderState();
        resolver().update(model, state, BlockDisplayContext.create());
        BlockModelAccess access = (BlockModelAccess) model;
        List<BlockStateModelPart> found = access.modelParts();
        List<double[]> corners = new ArrayList<>();
        for (BlockStateModelPart part : found) {
            for (Direction face : Direction.values()) {
                for (BakedQuad quad : part.getQuads(face)) {
                    for (int i = 0; i < BakedQuad.VERTEX_COUNT; i++) {
                        corners.add(at(quad.position(i), access.transformation()));
                    }
                }
            }
        }
        if (corners.isEmpty()) {
            return new Mesh(outlined(state), found.size() + " parts, fell back to its shape");
        }
        return new Mesh(corners, found.size() + " parts");
    }

    public record Mesh(List<double[]> corners, String parts) {
    }

    private static double[] at(org.joml.Vector3fc corner, Matrix4fc transformation) {
        if (transformation == null) {
            return new double[]{corner.x(), corner.y(), corner.z()};
        }
        Vector3f moved = new Vector3f(corner).mulPosition(transformation);
        return new double[]{moved.x(), moved.y(), moved.z()};
    }

    private static List<double[]> outlined(BlockState state) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return List.of();
        }
        VoxelShape shape = state.getShape(level, BlockPos.ZERO, CollisionContext.empty());
        List<double[]> corners = new ArrayList<>();
        if (shape.isEmpty()) {
            return corners;
        }
        for (AABB part : shape.toAabbs()) {
            corners.add(new double[]{part.minX, part.minY, part.minZ});
            corners.add(new double[]{part.maxX, part.minY, part.minZ});
            corners.add(new double[]{part.maxX, part.minY, part.maxZ});
            corners.add(new double[]{part.minX, part.minY, part.maxZ});
            corners.add(new double[]{part.minX, part.maxY, part.minZ});
            corners.add(new double[]{part.maxX, part.maxY, part.minZ});
            corners.add(new double[]{part.maxX, part.maxY, part.maxZ});
            corners.add(new double[]{part.minX, part.maxY, part.maxZ});
        }
        return corners;
    }

    private static BlockModelResolver resolver() {
        if (resolver == null) {
            resolver = new BlockModelResolver(Minecraft.getInstance().getModelManager());
        }
        return resolver;
    }
}