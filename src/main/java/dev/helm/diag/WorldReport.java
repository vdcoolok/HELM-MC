package dev.helm.diag;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;

public final class WorldReport {

    private static boolean settled;

    private WorldReport() {
    }

    public static void awaitPlayer() {
        if (settled) {
            return;
        }
        Minecraft client = Minecraft.getInstance();
        ClientLevel level = client.level;
        LocalPlayer player = client.player;
        if (level == null || player == null) {
            return;
        }
        BlockPos feet = player.blockPosition();
        if (feet.getX() == 0 && feet.getY() == 0 && feet.getZ() == 0) {
            Trace.instance().pulse("settled", "world",
                    "still waiting for the server to place the player");
            return;
        }
        settled = true;
        Trace.instance().event("world", "player at " + feet.getX() + " " + feet.getY() + " "
                + feet.getZ() + " yaw " + round(player.getYRot())
                + " pitch " + round(player.getXRot())
                + " onGround " + player.onGround()
                + " dimension " + level.dimension().identifier());
        Trace.instance().event("world", "chunk at feet loaded "
                + level.getChunkSource().hasChunk(feet.getX() >> 4, feet.getZ() >> 4));
        Trace.instance().barrier("world");
    }

    public static void restart() {
        settled = false;
    }

    private static String round(double value) {
        return String.format("%.2f", value);
    }
}
