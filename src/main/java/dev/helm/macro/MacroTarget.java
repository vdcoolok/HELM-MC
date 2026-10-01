package dev.helm.macro;

import java.util.Locale;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public final class MacroTarget {

    public static final String ARROW = " -> ";
    private static final String NONE = "-";

    private MacroTarget() {
    }

    public static String describe(String line) {
        if (line == null) {
            return "";
        }
        String trimmed = line.trim();
        String keyword = head(trimmed);
        String shown = switch (keyword) {
            case MacroSyntax.GOTO_HERE -> aimedBlock();
            case MacroSyntax.LOOKAT_HERE -> aimedAngles();
            default -> "";
        };
        return shown.isEmpty() ? trimmed : trimmed + ARROW + shown;
    }

    private static String head(String line) {
        int space = line.indexOf(' ');
        return (space < 0 ? line : line.substring(0, space)).toLowerCase(Locale.ROOT);
    }

    private static String aimedBlock() {
        BlockHitResult hit = aimed();
        if (hit == null) {
            return NONE;
        }
        var pos = hit.getBlockPos();
        return pos.getX() + " " + pos.getY() + " " + pos.getZ();
    }

    private static String aimedAngles() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return NONE;
        }
        return round(player.getXRot()) + " / " + round(player.getYRot());
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