package dev.helm.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.StagedVertexBuffer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import dev.helm.navigate.NavigatorAgent;
import dev.helm.setting.Settings;

public final class RouteOverlay {

    private static final StagedVertexBuffer BUFFER = new StagedVertexBuffer(() -> "HELM Path", 256);

    private RouteOverlay() {
    }

    public static void draw(PoseStack pose, CameraRenderState camera) {
        var agent = NavigatorAgent.instance();
        if (!agent.pilot().isWalking()) {
            return;
        }
        var route = agent.pilot().route();
        if (route == null) {
            return;
        }
        var settings = Settings.holder().path();
        ViewOffset view = ViewOffset.of(camera);
        RoutePainter painter = new RoutePainter(BUFFER, settings);
        if (settings.blocksToPlace()) {
            painter.paintBlocks(pose, route.blocksToPlace(), LineColour.PLACE, view);
        }
        painter.paint(pose, route, agent.pilot().stepIndex(), LineColour.PATH, view);
    }
}
