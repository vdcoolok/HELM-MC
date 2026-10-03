package dev.helm.outline.block;

import java.util.HashSet;
import java.util.Set;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public final class BlockSilhouetteDraw {

    private static final int LIMIT = 512;

    private static final Set<BlockPos> CLAIMED = new HashSet<>();
    private static final PoseStack POSE = new PoseStack();

    private BlockSilhouetteDraw() {
    }

    public static void submit(ClientLevel level, CameraRenderState camera,
                              SubmitNodeCollector collector) {
        if (level == null || camera == null || camera.pos == null) {
            return;
        }
        BlockSilhouetteModels models = BlockSilhouetteModels.instance();
        models.reset();
        CLAIMED.clear();
        int drawn = 0;
        for (BlockSilhouetteFeed feed : BlockSilhouetteFeeds.live()) {
            int colour = feed.colour();
            if (colour == 0) {
                continue;
            }
            for (BlockPos pos : feed.watch()) {
                if (drawn >= LIMIT) {
                    return;
                }
                if (CLAIMED.add(pos.immutable())
                        && drawAt(level, pos, camera.pos, collector, colour, models)) {
                    drawn++;
                }
            }
        }
    }

    private static boolean drawAt(ClientLevel level, BlockPos pos, Vec3 origin,
                                  SubmitNodeCollector collector, int colour,
                                  BlockSilhouetteModels models) {
        if (level.isOutsideBuildHeight(pos.getY())) {
            return false;
        }
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) {
            return false;
        }
        BlockModelRenderState model = models.of(state);
        if (model == null) {
            return false;
        }
        POSE.pushPose();
        POSE.translate(pos.getX() - origin.x(), pos.getY() - origin.y(),
                pos.getZ() - origin.z());
        model.submitOnlyOutline(POSE, collector, LightCoordsUtil.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY, ARGB.opaque(colour));
        POSE.popPose();
        return true;
    }
}
