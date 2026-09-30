package dev.helm.render;

import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;

public record ViewOffset(double x, double y, double z) {

    public static ViewOffset of(CameraRenderState camera) {
        if (camera == null) {
            return new ViewOffset(0, 0, 0);
        }
        Vec3 pos = camera.pos;
        return pos == null ? new ViewOffset(0, 0, 0)
                : new ViewOffset(pos.x, pos.y, pos.z);
    }

    public double applyX(double worldX) {
        return worldX - x;
    }

    public double applyY(double worldY) {
        return worldY - y;
    }

    public double applyZ(double worldZ) {
        return worldZ - z;
    }
}
