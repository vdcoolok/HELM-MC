package dev.helm.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.StagedVertexBuffer;
import net.minecraft.client.renderer.rendertype.RenderType;

public final class FillBatch {

    private final StagedVertexBuffer buffer;
    private final RenderType type;
    private final StagedVertexBuffer.Draw draw;
    private final PoseStack pose;
    private final VertexConsumer consumer;

    private float red = 1.0F;
    private float green = 1.0F;
    private float blue = 1.0F;
    private float alpha = 0.3F;

    public FillBatch(StagedVertexBuffer buffer, PoseStack pose, RenderType type) {
        this.buffer = buffer;
        this.type = type;
        this.pose = pose;
        this.draw = buffer.appendDraw(type.format(), type.primitiveTopology());
        this.consumer = buffer.getVertexBuilder(draw);
    }

    public FillBatch colour(float red, float green, float blue, float alpha) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
        return this;
    }

    public FillBatch triangle(double[] a, double[] b, double[] c) {
        PoseStack.Pose current = pose.last();
        vertex(current, a);
        vertex(current, b);
        vertex(current, c);
        return this;
    }

    public FillBatch quad(double[] a, double[] b, double[] c, double[] d) {
        return triangle(a, b, c).triangle(a, c, d);
    }

    public void flush() {
        buffer.upload();
        StagedVertexBuffer.ExecuteInfo info = buffer.getExecuteInfo(draw);
        if (info != null) {
            type.prepare().drawFromBuffer(info);
        }
        buffer.endFrame();
    }

    private void vertex(PoseStack.Pose current, double[] point) {
        consumer.addVertex(current, (float) point[0], (float) point[1], (float) point[2])
                .setColor(red, green, blue, alpha);
    }
}