package dev.helm.render;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.BlockStateModelSet;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Matrix4f;

public final class BlockSilhouette {

    private static final long SEED = 42L;
    private static final Matrix4f IDENTITY = new Matrix4f();
    private static BlockModelResolver resolver;
    private static BlockStateModelSet models;

    private BlockSilhouette() {
    }

    public static List<double[]> boxes(BlockState state) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return List.of();
        }
        VoxelShape shape = state.getShape(level, BlockPos.ZERO, CollisionContext.empty());
        if (shape.isEmpty()) {
            return List.of();
        }
        List<double[]> corners = new ArrayList<>();
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

    private static List<double[]> outlined(BlockState state) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return List.of();
        }
        List<double[]> corners = new ArrayList<>();
        VoxelShape shape = state.getShape(level, BlockPos.ZERO, CollisionContext.empty());
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

    private static BlockStateModelSet models() {
        if (models == null) {
            models = Minecraft.getInstance().getModelManager().getBlockStateModelSet();
        }
        return models;
    }
}
