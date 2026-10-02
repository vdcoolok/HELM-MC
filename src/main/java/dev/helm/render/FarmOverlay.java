package dev.helm.render;

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
import net.minecraft.world.phys.Vec3;

import dev.helm.diag.Trace;
import dev.helm.farm.FarmTask;
import dev.helm.setting.Settings;

public final class FarmOverlay {

    private static final StagedVertexBuffer BUFFER = new StagedVertexBuffer(() -> "HELM Farm", 128);
    private static final float ALPHA = 0.3F;
    private static final double NUDGE = 0.02D;

    private FarmOverlay() {
    }

    public static void draw(PoseStack pose, CameraRenderState camera) {
        try {
            paint(pose, camera);
        } catch (RuntimeException | LinkageError failure) {
            Trace.instance().pulse("farm-tint", "render", "tint gave up: " + failure);
        }
    }

    private static void paint(PoseStack pose, CameraRenderState camera) {
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
        RenderType fill = RouteRenderTypes.translucentFill();
        int crops = settings.renderTargets() ? paintCrops(pose, level, farm.harvestable(), fill,
                view, camera) : 0;
        int drops = settings.renderDrops() ? paintDrops(pose, level, farm, fill, view, camera) : 0;
        Trace.instance().repeat("farm-tint", "render", "tint " + crops + " crops and "
                + drops + " drops, each " + fill.format() + " " + fill.primitiveTopology());
    }

    private static int paintCrops(PoseStack pose, ClientLevel level, List<BlockPos> crops,
                                  RenderType fill, ViewOffset view, CameraRenderState camera) {
        if (crops.isEmpty()) {
            return 0;
        }
        FillBatch batch = batch(pose, fill, LineColour.GOAL);
        int drawn = 0;
        for (BlockPos crop : crops) {
            List<double[]> box = BlockSilhouette.boxes(level.getBlockState(crop));
            if (box.isEmpty()) {
                continue;
            }
            AABB bounds = bounds(box);
            Vec3 mid = bounds.getCenter();
            Vec3 centre = new Vec3(crop.getX() + mid.x, crop.getY() + mid.y,
                    crop.getZ() + mid.z);
            ShapeFill.corners(batch, Billboard.facing(camera, centre,
                    Math.max(bounds.getXsize(), bounds.getZsize()) / 2.0D + NUDGE,
                    bounds.getYsize() / 2.0D + NUDGE, 0.0D, view));
            drawn++;
        }
        batch.flush();
        return drawn;
    }

    private static int paintDrops(PoseStack pose, ClientLevel level, FarmTask farm,
                                  RenderType fill, ViewOffset view, CameraRenderState camera) {
        if (farm.harvestLedger().empty()) {
            return 0;
        }
        LineBatch batch = new LineBatch(BUFFER, pose, fill)
                .colour(LineColour.PLACE.red(), LineColour.PLACE.green(),
                        LineColour.PLACE.blue(), ALPHA)
                .width((float) Settings.holder().path().lineWidth());
        int drawn = 0;
        boolean first = true;
        for (Entity entity : level.entitiesForRendering()) {
            if (!(entity instanceof ItemEntity dropped)
                    || !farm.harvestLedger().wants(dropped.getItem())) {
                continue;
            }
            drawn += outlineDrop(batch, dropped, view, camera, first);
            first = false;
        }
        batch.flush();
        return drawn;
    }

    private static int outlineDrop(LineBatch batch, ItemEntity dropped, ViewOffset view,
                                   CameraRenderState camera, boolean first) {
        AABB box = ItemSilhouette.modelBox(dropped);
        List<double[]> corners = ItemSilhouette.outline(dropped);
        if (box == null || corners.isEmpty()) {
            return 0;
        }
        float rise = ItemBob.rise(dropped, ItemSilhouette.floorOf(box));
        float spin = ItemBob.spin(dropped);
        List<double[]> placed = new java.util.ArrayList<>(corners.size());
        for (double[] corner : corners) {
            placed.add(viewed(corner, dropped, rise, spin, view));
        }
        if (first) {
            Trace.instance().pulse("farm-tint-drop", "render", dropped.getDisplayName().getString()
                    + " mesh " + (corners.size() / 4) + " faces, rise " + round(rise));
        }
        MeshOutline.draw(batch, placed);
        return corners.size() / 4;
    }

    private static double[] viewed(double[] corner, ItemEntity dropped, float rise, float spin,
                                   ViewOffset view) {
        double angle = Math.toRadians(spin);
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double x = dropped.getX() + corner[0] * cos + corner[2] * sin;
        double y = dropped.getY() + corner[1] + rise;
        double z = dropped.getZ() - corner[0] * sin + corner[2] * cos;
        return new double[]{view.applyX(x), view.applyY(y), view.applyZ(z)};
    }

    private static AABB bounds(List<double[]> points) {
        double minX = points.get(0)[0];
        double maxX = minX;
        double minY = points.get(0)[1];
        double maxY = minY;
        double minZ = points.get(0)[2];
        double maxZ = minZ;
        for (double[] point : points) {
            minX = Math.min(minX, point[0]);
            maxX = Math.max(maxX, point[0]);
            minY = Math.min(minY, point[1]);
            maxY = Math.max(maxY, point[1]);
            minZ = Math.min(minZ, point[2]);
            maxZ = Math.max(maxZ, point[2]);
        }
        return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    private static String round(double value) {
        return String.format("%.3f", value);
    }

    private static FillBatch batch(PoseStack pose, RenderType fill, LineColour colour) {
        return new FillBatch(BUFFER, pose, fill)
                .colour(colour.red(), colour.green(), colour.blue(), ALPHA);
    }
}