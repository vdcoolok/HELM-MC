package dev.helm.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.StagedVertexBuffer;

import dev.helm.navigate.NavigatorAgent;
import dev.helm.setting.Settings;

public final class RouteOverlay {

    private static final StagedVertexBuffer BUFFER = new StagedVertexBuffer(() -> "HELM Path", 256);

    private RouteOverlay() {
    }

    public static void draw(PoseStack pose) {
        var agent = NavigatorAgent.instance();
        if (!agent.pilot().isWalking()) {
            return;
        }
        var route = agent.pilot().route();
        var settings = Settings.holder().path();
        RoutePainter painter = new RoutePainter(BUFFER, settings);
        if (settings.renderBlocksToBreak()) {
            painter.paintBlocks(pose, route.blocksToBreak(), LineColour.BREAK);
        }
        if (settings.renderBlocksToPlace()) {
            painter.paintBlocks(pose, route.blocksToPlace(), LineColour.PLACE);
        }
        painter.paint(pose, route, 0, LineColour.PATH);
    }
}
