package dev.helm.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.StagedVertexBuffer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import dev.helm.movement.Route;
import dev.helm.movement.step.PlanStep;
import dev.helm.setting.PathSettings;

public final class RoutePainter {

    private static final double NODE_OFFSET = 0.5D;
    private static final double TUBE_EXTRA = 0.03D;
    private static final double BOX_EXPAND = 0.002D;
    private static final int FADE_NEAR = 10;
    private static final int FADE_FAR = 20;
    private static final float FULL_ALPHA = 0.4F;
    private static final int BEHIND_STEPS = 3;

    private final StagedVertexBuffer buffer;
    private final PathSettings settings;

    public RoutePainter(StagedVertexBuffer buffer, PathSettings settings) {
        this.buffer = buffer;
        this.settings = settings;
    }

    public void paint(PoseStack pose, Route route, int fromStep, LineColour colour) {
        if (!settings.renderPath() || route.steps().isEmpty()) {
            return;
        }
        LineBatch batch = new LineBatch(buffer, pose).width((float) settings.lineWidth());
        drawRoute(batch, route, fromStep, colour);
        batch.flush();
    }

    private void drawRoute(LineBatch batch, Route route, int fromStep, LineColour colour) {
        int start = Math.max(fromStep - BEHIND_STEPS, 0);
        int count = route.length();
        for (int index = start; index < count - 1; index++) {
            PlanStep step = route.at(index);
            PlanStep next = route.at(index + 1);
            batch.colour(colour.red(), colour.green(), colour.blue(),
                    alphaFor(index, start, colour));
            emitStep(batch, step, next);
        }
    }

    private float alphaFor(int index, int start, LineColour colour) {
        if (!settings.fadePath()) {
            return colour.alpha() > 0 ? colour.alpha() : FULL_ALPHA;
        }
        int near = FADE_NEAR + start;
        int far = FADE_FAR + start;
        if (index <= near) {
            return FULL_ALPHA;
        }
        if (index > far) {
            return 0.0F;
        }
        return FULL_ALPHA * (1.0F - (float) (index - near) / (float) (far - near));
    }

    private void emitStep(LineBatch batch, PlanStep from, PlanStep to) {
        double x1 = from.toX() + NODE_OFFSET;
        double y1 = from.toY() + NODE_OFFSET;
        double z1 = from.toZ() + NODE_OFFSET;
        double x2 = to.toX() + NODE_OFFSET;
        double y2 = to.toY() + NODE_OFFSET;
        double z2 = to.toZ() + NODE_OFFSET;
        batch.segment(x1, y1, z1, x2, y2, z2);
        if (settings.renderPathAsLine()) {
            return;
        }
        double top = TUBE_EXTRA + NODE_OFFSET;
        batch.segment(x2, y2, z2, x2, top, z2);
        batch.segment(x2, top, z2, x1, top, z1);
        batch.segment(x1, top, z1, x1, y1, z1);
    }

    public void paintBlocks(PoseStack pose, java.util.Set<int[]> blocks, LineColour colour) {
        if (blocks.isEmpty()) {
            return;
        }
        LineBatch batch = new LineBatch(buffer, pose)
                .colour(colour.red(), colour.green(), colour.blue(), FULL_ALPHA)
                .width(2.0F);
        for (int[] position : blocks) {
            batch.box(shapeAt(position).inflate(BOX_EXPAND));
        }
        batch.flush();
    }

    private AABB shapeAt(int[] position) {
        var client = net.minecraft.client.Minecraft.getInstance();
        if (client == null || client.level == null) {
            return new AABB(position[0], position[1], position[2],
                    position[0] + 1, position[1] + 1, position[2] + 1);
        }
        var pos = new net.minecraft.core.BlockPos(position[0], position[1], position[2]);
        VoxelShape shape = client.level.getBlockState(pos).getCollisionShape(client.level, pos);
        if (shape.isEmpty()) {
            shape = Shapes.block();
        }
        return shape.bounds().move(pos);
    }
}
