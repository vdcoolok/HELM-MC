package dev.helm.render;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.StagedVertexBuffer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;

import dev.helm.diag.Trace;
import dev.helm.farm.FarmTask;
import dev.helm.setting.Settings;

public final class FarmOverlay {

    private static final StagedVertexBuffer BUFFER = new StagedVertexBuffer(() -> "HELM Farm", 128);
    private static final float ALPHA = 1.0F;
    private static final LineColour TINT = LineColour.of(0xFFFF2020);

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

        int crops = 0;
        int drops = 0;
        if (settings.renderTargets()) {
            crops = paintCrops(pose, level, farm.harvestable(), fill, view);
        }
        if (settings.renderDrops()) {
            drops = paintDrops(pose, level, farm, fill, view);
        }
        Trace.instance().repeat("farm-tint", "render", "tint " + fill.format() + " "
                + fill.primitiveTopology() + " " + crops + " crop faces and " + drops
                + " drop corners");
    }

    private static int paintCrops(PoseStack pose, ClientLevel level, List<BlockPos> crops,
                                  RenderType fill, ViewOffset view) {
        if (crops.isEmpty()) {
            return 0;
        }
        FillBatch batch = batch(pose, fill, TINT);
        int drawn = 0;
        boolean first = true;
        for (BlockPos crop : crops) {
            List<double[]> quads = BlockSilhouette.quads(level.getBlockState(crop));
            BlockSilhouette.fill(batch, Silhouette.at(crop.getX(), crop.getY(), crop.getZ(),
                    quads, view));
            drawn += quads.size() / 4;
            if (first) {
                first = false;
                Trace.instance().pulse("farm-tint-crop", "render", describe(level.getBlockState(crop),
                        Silhouette.at(crop.getX(), crop.getY(), crop.getZ(), quads, view)));
            }
        }
        batch.flush();
        return drawn;
    }

    private static int paintDrops(PoseStack pose, ClientLevel level, FarmTask farm,
                                  RenderType fill, ViewOffset view) {
        if (farm.harvestLedger().empty()) {
            return 0;
        }
        FillBatch batch = batch(pose, fill, TINT);
        int drawn = 0;
        boolean first = true;
        for (Entity entity : level.entitiesForRendering()) {
            if (!(entity instanceof ItemEntity dropped)
                    || !farm.harvestLedger().wants(dropped.getItem())) {
                continue;
            }
            drawn += fillDrop(batch, dropped, view, first);
            first = false;
        }
        batch.flush();
        return drawn;
    }

    private static int fillDrop(FillBatch batch, ItemEntity dropped, ViewOffset view,
                                boolean first) {
        List<double[]> model = ItemSilhouette.corners(dropped);
        if (model.isEmpty()) {
            return 0;
        }
        float rise = ItemBob.rise(dropped, ItemSilhouette.modelFloor(model));
        List<double[]> placed = Silhouette.at(dropped.getX(), dropped.getY(), dropped.getZ(),
                Silhouette.spun(model, ItemBob.spin(dropped), rise), view);
        if (first) {
            Trace.instance().pulse("farm-tint-drop", "render",
                    dropped.getDisplayName().getString() + " " + describe(placed));
        }
        ShapeFill.corners(batch, placed);
        return model.size();
    }

    private static String describe(BlockState state, List<double[]> placed) {
        return state.getBlock().toString() + " " + describe(placed);
    }

    private static String describe(List<double[]> points) {
        if (points.isEmpty()) {
            return "no corners";
        }
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
        return points.size() + " corners span x " + round(minX) + " to " + round(maxX)
                + ", y " + round(minY) + " to " + round(maxY)
                + ", z " + round(minZ) + " to " + round(maxZ);
    }

    private static String round(double value) {
        return String.format("%.3f", value);
    }

    private static FillBatch batch(PoseStack pose, RenderType fill, LineColour colour) {
        return new FillBatch(BUFFER, pose, fill)
                .colour(colour.red(), colour.green(), colour.blue(), ALPHA);
    }
}