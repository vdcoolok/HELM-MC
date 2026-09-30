package dev.helm.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.StagedVertexBuffer;
import net.minecraft.world.phys.AABB;
import dev.helm.movement.Route;
import dev.helm.movement.step.PlanStep;
import dev.helm.setting.PathSettings;

public final class RoutePainter {

    private static final double NODE_OFFSET = 0.5D;
    private static final double TUBE_EXTRA = 0.03D;
    private static final double BOX_EXPAND = 0.002D;
    private static final float LINE_ALPHA = 0.4F;
    private static final int FADE_NEAR = 10;
    private static final int FADE_FAR = 20;
    private static final int BEHIND_STEPS = 3;

    private final StagedVertexBuffer buffer;
    private final PathSettings settings;
    private int first;

    public RoutePainter(StagedVertexBuffer buffer, PathSettings settings) {
        this.buffer = buffer;
        this.settings = settings;
    }

    public void paint(PoseStack pose, Route route, int fromStep, LineColour colour,
                      ViewOffset view) {
        if (!settings.renderPath() || route.steps().isEmpty()) {
            return;
        }
        first = Math.max(fromStep - BEHIND_STEPS, 0);
        LineBatch batch = new LineBatch(buffer, pose,
                RouteRenderTypes.forPath(settings.pathIgnoreDepth()))
                .width((float) settings.lineWidth());
        drawRoute(batch, route, colour, view);
        batch.flush();
    }

    public void paintBlocks(PoseStack pose, java.util.Set<int[]> blocks, LineColour colour,
                            ViewOffset view) {
        if (blocks.isEmpty()) {
            return;
        }
        LineBatch batch = new LineBatch(buffer, pose,
                RouteRenderTypes.forPath(settings.blocksIgnoreDepth()))
                .colour(colour.red(), colour.green(), colour.blue(), LINE_ALPHA)
                .width((float) settings.lineWidth());
        for (int[] position : blocks) {
            batch.box(viewed(outlineAt(position), view).inflate(BOX_EXPAND));
        }
        batch.flush();
    }

    private void drawRoute(LineBatch batch, Route route, LineColour colour, ViewOffset view) {
        int count = route.length();
        int index = first;
        int fadeNear = FADE_NEAR + first;
        int fadeFar = FADE_FAR + first;
        boolean fading = settings.fadePath();
        while (index < count - 1) {
            int next = index + 1;
            PlanStep from = route.at(index);
            PlanStep to = route.at(next);
            int stepX = to.toX() - from.toX();
            int stepY = to.toY() - from.toY();
            int stepZ = to.toZ() - from.toZ();
            while (next + 1 < count && (!fading || next + 1 < fadeNear)
                    && stepX == route.at(next + 1).toX() - to.toX()
                    && stepY == route.at(next + 1).toY() - to.toY()
                    && stepZ == route.at(next + 1).toZ() - to.toZ()) {
                to = route.at(++next);
            }
            float alpha = alphaAt(index, fadeNear, fadeFar);
            if (alpha <= 0.0F) {
                return;
            }
            batch.colour(colour.red(), colour.green(), colour.blue(), alpha);
            emitStep(batch, from, to, view);
            index = next;
        }
    }

    private float alphaAt(int index, int fadeNear, int fadeFar) {
        if (!settings.fadePath()) {
            return LINE_ALPHA;
        }
        if (index <= fadeNear) {
            return LINE_ALPHA;
        }
        if (index > fadeFar) {
            return 0.0F;
        }
        return LINE_ALPHA * (1.0F - (float) (index - fadeNear) / (float) (fadeFar - fadeNear));
    }

    private void emitStep(LineBatch batch, PlanStep from, PlanStep to, ViewOffset view) {
        double x1 = view.applyX(from.toX() + NODE_OFFSET);
        double y1 = view.applyY(from.toY() + NODE_OFFSET);
        double z1 = view.applyZ(from.toZ() + NODE_OFFSET);
        double x2 = view.applyX(to.toX() + NODE_OFFSET);
        double y2 = view.applyY(to.toY() + NODE_OFFSET);
        double z2 = view.applyZ(to.toZ() + NODE_OFFSET);
        batch.segment(x1, y1, z1, x2, y2, z2);
        if (settings.renderPathAsLine()) {
            return;
        }
        double top1 = y1 + TUBE_EXTRA;
        double top2 = y2 + TUBE_EXTRA;
        batch.segment(x2, y2, z2, x2, top2, z2);
        batch.segment(x2, top2, z2, x1, top1, z1);
        batch.segment(x1, top1, z1, x1, y1, z1);
    }

    private AABB viewed(AABB bounds, ViewOffset view) {
        return new AABB(view.applyX(bounds.minX), view.applyY(bounds.minY),
                view.applyZ(bounds.minZ), view.applyX(bounds.maxX),
                view.applyY(bounds.maxY), view.applyZ(bounds.maxZ));
    }

    private AABB outlineAt(int[] position) {
        AABB outline = BlockOutlines.at(position);
        return outline == null ? new AABB(position[0], position[1], position[2],
                position[0] + 1, position[1] + 1, position[2] + 1) : outline;
    }
}
