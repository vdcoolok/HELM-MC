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
                + fill.primitiveTopology() + " " + crops + " crops and " + drops + " drops");
    }

    private static int paintCrops(PoseStack pose, ClientLevel level, List<BlockPos> crops,
                                  RenderType fill, ViewOffset view) {
        if (crops.isEmpty()) {
            return 0;
        }
        FillBatch batch = batch(pose, fill, LineColour.GOAL);
        int drawn = 0;
        for (BlockPos crop : crops) {
            List<double[]> quads = BlockSilhouette.quads(level.getBlockState(crop));
            BlockSilhouette.fill(batch, Silhouette.at(crop.getX(), crop.getY(), crop.getZ(),
                    quads, view));
            drawn += quads.size() / 4;
            if (drawn == 0 && crops.size() == 1) {
                Trace.instance().pulse("farm-tint-quads", "render", "first crop "
                        + crop.getX() + " " + crop.getY() + " " + crop.getZ() + " gave "
                        + quads.size() + " corners");
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
        FillBatch batch = batch(pose, fill, LineColour.PLACE);
        int drawn = 0;
        for (Entity entity : level.entitiesForRendering()) {
            if (!(entity instanceof ItemEntity dropped)
                    || !farm.harvestLedger().wants(dropped.getItem())) {
                continue;
            }
            drawn += fillDrop(batch, dropped, view);
        }
        batch.flush();
        return drawn;
    }

    private static int fillDrop(FillBatch batch, ItemEntity dropped, ViewOffset view) {
        List<double[]> corners = ItemSilhouette.corners(dropped);
        if (corners.size() < 3) {
            return 0;
        }
        float rise = ItemBob.rise(dropped, ItemSilhouette.modelFloor(corners));
        List<double[]> spun = Silhouette.spun(corners, ItemBob.spin(dropped), rise);
        ShapeFill.corners(batch, Silhouette.at(dropped.getX(), dropped.getY(), dropped.getZ(),
                spun, view));
        return corners.size();
    }

    private static FillBatch batch(PoseStack pose, RenderType fill, LineColour colour) {
        return new FillBatch(BUFFER, pose, fill)
                .colour(colour.red(), colour.green(), colour.blue(), ALPHA);
    }
}