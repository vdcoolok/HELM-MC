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
import com.mojang.math.Axis;
import net.minecraft.world.phys.AABB;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import dev.helm.diag.Trace;
import dev.helm.farm.FarmTask;
import dev.helm.setting.Settings;

public final class FarmOverlay {

    private static final StagedVertexBuffer BUFFER = new StagedVertexBuffer(() -> "HELM Farm", 128);
    private static final float ALPHA = 0.3F;

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
        RenderType lines = RouteRenderTypes.forPath(true);
        int crops = settings.renderTargets() ? paintCrops(pose, level, farm.harvestable(), lines, view) : 0;
        int drops = settings.renderDrops() ? paintDrops(pose, level, farm, lines, view) : 0;
        Trace.instance().repeat("farm-tint", "render", "tint " + crops + " crops and "
                + drops + " drops");
    }

    private static int paintCrops(PoseStack pose, ClientLevel level, List<BlockPos> crops,
                                  RenderType lines, ViewOffset view) {
        if (crops.isEmpty()) {
            return 0;
        }
        LineBatch batch = new LineBatch(BUFFER, pose, lines)
                .colour(LineColour.GOAL.red(), LineColour.GOAL.green(),
                        LineColour.GOAL.blue(), ALPHA)
                .width((float) Settings.holder().path().lineWidth());
        int drawn = 0;
        boolean first = true;
        for (BlockPos crop : crops) {
            BlockSilhouette.Mesh mesh = BlockSilhouette.mesh(level.getBlockState(crop));
            List<double[]> quads = mesh.corners();
            if (quads.isEmpty()) {
                continue;
            }
            List<double[]> placed = new java.util.ArrayList<>(quads.size());
            for (double[] corner : quads) {
                placed.add(new double[]{view.applyX(corner[0] + crop.getX()),
                        view.applyY(corner[1] + crop.getY()),
                        view.applyZ(corner[2] + crop.getZ())});
            }
            if (first) {
                first = false;
                AABB box = ShapeOutline.bounds(placed);
                Trace.instance().pulse("farm-tint-crop", "render",
                        level.getBlockState(crop).getBlock().toString() + " outline "
                                + (placed.size() / 4) + " faces, box " + round(box.getXsize())
                                + " by " + round(box.getYsize()) + " by " + round(box.getZsize())
                                + ", floor " + round(box.minY) + " from " + mesh.parts());
            }
            ShapeOutline.each(batch, placed);
            drawn += placed.size() / 4;
        }
        batch.flush();
        return drawn;
    }

    private static int paintDrops(PoseStack pose, ClientLevel level, FarmTask farm,
                                  RenderType lines, ViewOffset view) {
        if (farm.harvestLedger().empty()) {
            return 0;
        }
        LineBatch batch = new LineBatch(BUFFER, pose, lines)
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
            drawn += outlineDrop(batch, dropped, view, first);
            first = false;
        }
        batch.flush();
        return drawn;
    }

    private static int outlineDrop(LineBatch batch, ItemEntity dropped, ViewOffset view, boolean first) {
        List<double[]> corners = ItemSilhouette.corners(dropped);
        if (corners.isEmpty()) {
            return 0;
        }
        float rise = ItemBob.rise(dropped, ShapeOutline.lowest(corners));
        float spin = ItemBob.spin(dropped);
        List<double[]> placed = new java.util.ArrayList<>(corners.size());
        for (double[] corner : corners) {
            placed.add(viewed(corner, dropped, rise, spin, view));
        }
        if (first) {
            AABB box = ShapeOutline.bounds(corners);
            Trace.instance().pulse("farm-tint-drop", "render", dropped.getDisplayName().getString()
                    + " outline " + corners.size() + " corners, box " + round(box.getXsize())
                    + " by " + round(box.getYsize()) + " by " + round(box.getZsize())
                    + ", floor " + round(box.minY) + ", rise " + round(rise) + ", spin " + round(spin));
        }
        ShapeOutline.draw(batch, placed);
        return corners.size() / 4;
    }

    private static double[] viewed(double[] corner, ItemEntity dropped, float rise, float spin,
                                   ViewOffset view) {
        Quaternionf turn = Axis.YP.rotation(spin);
        Vector3f at = new Vector3f((float) corner[0], (float) corner[1], (float) corner[2]);
        at.rotate(turn);
        double x = dropped.getX() + at.x();
        double y = dropped.getY() + at.y() + rise;
        double z = dropped.getZ() + at.z();
        return new double[]{view.applyX(x), view.applyY(y), view.applyZ(z)};
    }

    private static String round(double value) {
        return String.format("%.3f", value);
    }

}
