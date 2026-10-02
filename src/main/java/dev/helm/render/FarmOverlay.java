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

import dev.helm.farm.FarmTask;
import dev.helm.setting.Settings;

public final class FarmOverlay {

    private static final StagedVertexBuffer BUFFER = new StagedVertexBuffer(() -> "HELM Farm", 128);
    private static final float ALPHA = 0.3F;

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
        RenderType fill = RouteRenderTypes.translucentFill();

        if (settings.renderTargets()) {
            paintCrops(pose, level, farm.harvestable(), fill, view);
        }
        if (settings.renderDrops()) {
            paintDrops(pose, level, farm, fill, view);
        }
    }

    private static void paintCrops(PoseStack pose, ClientLevel level, List<BlockPos> crops,
                                   RenderType fill, ViewOffset view) {
        if (crops.isEmpty()) {
            return;
        }
        FillBatch batch = batch(pose, fill, LineColour.GOAL);
        for (BlockPos crop : crops) {
            List<double[]> quads = BlockSilhouette.quads(level.getBlockState(crop));
            BlockSilhouette.fill(batch, Silhouette.at(crop.getX(), crop.getY(), crop.getZ(),
                    quads, view));
        }
        batch.flush();
    }

    private static void paintDrops(PoseStack pose, ClientLevel level, FarmTask farm,
                                   RenderType fill, ViewOffset view) {
        if (farm.harvestLedger().empty()) {
            return;
        }
        FillBatch batch = batch(pose, fill, LineColour.PLACE);
        for (Entity entity : level.entitiesForRendering()) {
            if (!(entity instanceof ItemEntity dropped)
                    || !farm.harvestLedger().wants(dropped.getItem())) {
                continue;
            }
            fillDrop(batch, dropped, view);
        }
        batch.flush();
    }

    private static void fillDrop(FillBatch batch, ItemEntity dropped, ViewOffset view) {
        List<double[]> corners = ItemSilhouette.corners(dropped);
        if (corners.size() < 3) {
            return;
        }
        float rise = ItemBob.rise(dropped, ItemSilhouette.modelFloor(corners));
        List<double[]> spun = Silhouette.spun(corners, ItemBob.spin(dropped), rise);
        ShapeFill.corners(batch, Silhouette.at(dropped.getX(), dropped.getY(), dropped.getZ(),
                spun, view));
    }

    private static FillBatch batch(PoseStack pose, RenderType fill, LineColour colour) {
        return new FillBatch(BUFFER, pose, fill)
                .colour(colour.red(), colour.green(), colour.blue(), ALPHA);
    }
}