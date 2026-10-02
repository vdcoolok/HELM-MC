package dev.helm.render;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.StagedVertexBuffer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;

import dev.helm.farm.FarmTask;
import dev.helm.setting.Settings;

public final class FarmOverlay {

    private static final StagedVertexBuffer BUFFER = new StagedVertexBuffer(() -> "HELM Farm", 128);
    private static final float ALPHA = 0.55F;
    private static final double BOX_EXPAND = 0.006D;

    private FarmOverlay() {
    }

    public static void draw(PoseStack pose, CameraRenderState camera) {
        FarmTask farm = FarmTask.instance();
        if (!farm.running()) {
            return;
        }
        var settings = Settings.holder().farm();
        if (!settings.renderTargets() && !settings.renderDrops()) {
            return;
        }
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        ViewOffset view = ViewOffset.of(camera);
        float width = (float) Settings.holder().path().lineWidth();
        var type = RouteRenderTypes.forPath(Settings.holder().path().blocksIgnoreDepth());

        if (settings.renderTargets()) {
            paintCrops(pose, farm.harvestable(), width, type, view);
        }
        if (settings.renderDrops()) {
            paintDrops(pose, level, farm, width, type, view);
        }
    }

    private static void paintCrops(PoseStack pose, List<BlockPos> crops, float width,
                                   RenderType type,
                                   ViewOffset view) {
        if (crops.isEmpty()) {
            return;
        }
        LineBatch batch = batch(pose, width, type, LineColour.GOAL);
        for (BlockPos crop : crops) {
            for (AABB part : BlockOutlines.boxesAt(new int[]{crop.getX(), crop.getY(), crop.getZ()})) {
                batch.box(shifted(part, view).inflate(BOX_EXPAND));
            }
        }
        batch.flush();
    }

    private static void paintDrops(PoseStack pose, ClientLevel level, FarmTask farm, float width,
                                   RenderType type,
                                   ViewOffset view) {
        if (farm.harvestLedger().empty()) {
            return;
        }
        LineBatch batch = batch(pose, width, type, LineColour.PLACE);
        for (Entity entity : level.entitiesForRendering()) {
            if (!(entity instanceof ItemEntity dropped)
                    || !farm.harvestLedger().wants(dropped.getItem())) {
                continue;
            }
            DropSprite.outline(batch, shifted(DropSprite.corners(dropped), view));
        }
        batch.flush();
    }

    private static LineBatch batch(PoseStack pose, float width,
                                   RenderType type,
                                   LineColour colour) {
        return new LineBatch(BUFFER, pose, type)
                .colour(colour.red(), colour.green(), colour.blue(), ALPHA)
                .width(width);
    }

    private static AABB shifted(AABB bounds, ViewOffset view) {
        return new AABB(view.applyX(bounds.minX), view.applyY(bounds.minY),
                view.applyZ(bounds.minZ), view.applyX(bounds.maxX),
                view.applyY(bounds.maxY), view.applyZ(bounds.maxZ));
    }

    private static List<double[]> shifted(List<double[]> corners, ViewOffset view) {
        List<double[]> moved = new ArrayList<>(corners.size());
        for (double[] corner : corners) {
            moved.add(new double[]{view.applyX(corner[0]), view.applyY(corner[1]),
                    view.applyZ(corner[2])});
        }
        return moved;
    }
}