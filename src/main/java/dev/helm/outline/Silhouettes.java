package dev.helm.outline;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;

import dev.helm.outline.block.BlockSilhouetteDraw;
import dev.helm.outline.block.BlockSilhouetteFeeds;
import dev.helm.outline.block.BreakingSilhouette;
import dev.helm.outline.block.MineTargetSilhouette;
import dev.helm.outline.block.RipeCropSilhouette;
import dev.helm.outline.block.RouteBreakSilhouette;
import dev.helm.setting.Settings;

public final class Silhouettes {

    private static boolean installed;

    private Silhouettes() {
    }

    public static void install() {
        if (installed) {
            return;
        }
        installed = true;
        BlockSilhouetteFeeds.add(RouteBreakSilhouette.instance());
        BlockSilhouetteFeeds.add(BreakingSilhouette.instance());
        BlockSilhouetteFeeds.add(RipeCropSilhouette.instance());
        BlockSilhouetteFeeds.add(MineTargetSilhouette.instance());
    }

    public static void draw(LevelRenderState level, SubmitNodeCollector collector) {
        if (!Settings.holder().outline().enabled()) {
            return;
        }
        ClientLevel client = Minecraft.getInstance().level;
        CameraRenderState camera = level == null ? null : level.cameraRenderState;
        BlockSilhouetteDraw.submit(client, camera, collector);
    }
}
