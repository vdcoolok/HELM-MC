package dev.helm.render;

import java.util.List;

import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public final class Billboard {

    private Billboard() {
    }

    public static List<double[]> facing(CameraRenderState camera, Vec3 centre,
                                        double halfWidth, double halfHeight,
                                        double depth, ViewOffset view) {
        Vector3f right = axis(camera, true);
        Vector3f up = axis(camera, false);
        double[] r = {right.x(), right.y(), right.z()};
        double[] u = {up.x(), up.y(), up.z()};
        double x = centre.x;
        double y = centre.y;
        double z = centre.z;
        List<double[]> corners = new java.util.ArrayList<>(4);
        corners.add(corner(x, y, z, r, u, -halfWidth, halfHeight, depth, view));
        corners.add(corner(x, y, z, r, u, halfWidth, halfHeight, depth, view));
        corners.add(corner(x, y, z, r, u, halfWidth, -halfHeight, depth, view));
        corners.add(corner(x, y, z, r, u, -halfWidth, -halfHeight, depth, view));
        return corners;
    }

    public static double[] corner(double x, double y, double z, double[] right, double[] up,
                                  double across, double above, double depth, ViewOffset view) {
        double px = x + right[0] * across + up[0] * above + right[0] * depth;
        double py = y + right[1] * across + up[1] * above + right[1] * depth;
        double pz = z + right[2] * across + up[2] * above + right[2] * depth;
        return new double[]{view.applyX(px), view.applyY(py), view.applyZ(pz)};
    }

    private static Vector3f axis(CameraRenderState camera, boolean right) {
        Vector3f axis = new Vector3f(right ? 1.0F : 0.0F, right ? 0.0F : 1.0F, 0.0F);
        if (camera == null || camera.orientation == null) {
            return axis;
        }
        return right ? camera.orientation.transformPositiveX(axis)
                : camera.orientation.transformPositiveY(axis);
    }
}