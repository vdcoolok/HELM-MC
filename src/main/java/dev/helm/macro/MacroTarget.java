package dev.helm.macro;

import java.util.Locale;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public final class MacroTarget {

    private static final String GOTO = "goto";
    private static final String LOOKAT = "lookat";

    private MacroTarget() {
    }

    public static String resolve(String line) {
        String trimmed = line == null ? "" : line.trim();
        String keyword = head(trimmed);
        if (!MacroSyntax.GOTO_HERE.equals(keyword) && !MacroSyntax.LOOKAT_HERE.equals(keyword)) {
            return trimmed;
        }
        if (!tail(trimmed).isEmpty()) {
            return trimmed;
        }
        String aimed = MacroSyntax.GOTO_HERE.equals(keyword) ? aimedBlock() : aimedAngles();
        if (aimed == null) {
            throw new MacroSyntaxException("'" + keyword + "' needs something under the crosshair");
        }
        return GOTO + " " + aimed;
    }

    private static String head(String line) {
        int space = line.indexOf(' ');
        return (space < 0 ? line : line.substring(0, space)).toLowerCase(Locale.ROOT);
    }

    private static String tail(String line) {
        int space = line.indexOf(' ');
        return space < 0 ? "" : line.substring(space + 1).trim();
    }

    private static String aimedBlock() {
        BlockHitResult hit = aimed();
        if (hit == null) {
            return null;
        }
        var pos = hit.getBlockPos();
        return pos.getX() + " " + pos.getY() + " " + pos.getZ();
    }

    private static String aimedAngles() {
        var player = Minecraft.getInstance().player;
        if (player == null) {
            return null;
        }
        return round(player.getXRot()) + "/" + round(player.getYRot());
    }

    private static BlockHitResult aimed() {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null) {
            return null;
        }
        HitResult trace = client.hitResult;
        if (trace == null || trace.getType() != HitResult.Type.BLOCK) {
            return null;
        }
        return (BlockHitResult) trace;
    }

    private static String round(float angle) {
        return String.format(Locale.ROOT, "%.1f", angle);
    }
}