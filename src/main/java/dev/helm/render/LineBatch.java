package dev.helm.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.StagedVertexBuffer;
import net.minecraft.world.phys.AABB;

public final class LineBatch {

    private final StagedVertexBuffer buffer;
    private final StagedVertexBuffer.Draw draw;
    private final PoseStack pose;
    private VertexConsumer consumer;

    private float red = 1.0F;
    private float green = 1.0F;
    private float blue = 1.0F;
    private float alpha = 0.4F;
    private float width = 5.0F;

    public LineBatch(StagedVertexBuffer buffer, PoseStack pose) {
        this.buffer = buffer;
        this.pose = pose;
        this.draw = LinePipelines.begin(buffer);
        this.consumer = buffer.getVertexBuilder(draw);
    }

    public LineBatch colour(float red, float green, float blue, float alpha) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
        return this;
    }

    public LineBatch width(float width) {
        this.width = width;
        return this;
    }

    public LineBatch segment(double x1, double y1, double z1,
                             double x2, double y2, double z2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double dz = z2 - z1;
        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (length == 0.0D) {
            return this;
        }
        float nx = (float) (dx / length);
        float ny = (float) (dy / length);
        float nz = (float) (dz / length);
        return segment(x1, y1, z1, x2, y2, z2, nx, ny, nz);
    }

    public LineBatch segment(double x1, double y1, double z1,
                             double x2, double y2, double z2,
                             float nx, float ny, float nz) {
        PoseStack.Pose current = pose.last();
        consumer.addVertex(current, (float) x1, (float) y1, (float) z1)
                .setColor(red, green, blue, alpha)
                .setNormal(current, nx, ny, nz)
                .setLineWidth(width);
        consumer.addVertex(current, (float) x2, (float) y2, (float) z2)
                .setColor(red, green, blue, alpha)
                .setNormal(current, nx, ny, nz)
                .setLineWidth(width);
        return this;
    }

    public LineBatch box(AABB bounds) {
        double minX = bounds.minX;
        double minY = bounds.minY;
        double minZ = bounds.minZ;
        double maxX = bounds.maxX;
        double maxY = bounds.maxY;
        double maxZ = bounds.maxZ;

        segment(minX, minY, minZ, maxX, minY, minZ, 1.0F, 0.0F, 0.0F);
        segment(maxX, minY, minZ, maxX, minY, maxZ, 0.0F, 0.0F, 1.0F);
        segment(maxX, minY, maxZ, minX, minY, maxZ, -1.0F, 0.0F, 0.0F);
        segment(minX, minY, maxZ, minX, minY, minZ, 0.0F, 0.0F, -1.0F);

        segment(minX, maxY, minZ, maxX, maxY, minZ, 1.0F, 0.0F, 0.0F);
        segment(maxX, maxY, minZ, maxX, maxY, maxZ, 0.0F, 0.0F, 1.0F);
        segment(maxX, maxY, maxZ, minX, maxY, maxZ, -1.0F, 0.0F, 0.0F);
        segment(minX, maxY, maxZ, minX, maxY, minZ, 0.0F, 0.0F, -1.0F);

        segment(minX, minY, minZ, minX, maxY, minZ, 0.0F, 1.0F, 0.0F);
        segment(maxX, minY, minZ, maxX, maxY, minZ, 0.0F, 1.0F, 0.0F);
        segment(maxX, minY, maxZ, maxX, maxY, maxZ, 0.0F, 1.0F, 0.0F);
        segment(minX, minY, maxZ, minX, maxY, maxZ, 0.0F, 1.0F, 0.0F);
        return this;
    }

    public void flush(boolean ignoreDepth) {
        buffer.upload();
        StagedVertexBuffer.ExecuteInfo info = buffer.getExecuteInfo(draw);
        if (info != null) {
            LinePipelines.forDepth(ignoreDepth).prepare().drawFromBuffer(info);
        }
        buffer.endFrame();
    }
}
