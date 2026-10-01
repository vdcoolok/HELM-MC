package dev.helm.macro;

import java.util.Locale;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

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
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            throw new MacroSyntaxException("'" + keyword + "' needs to be typed while playing");
        }
        String captured = MacroSyntax.GOTO_HERE.equals(keyword) ? standingAt(player) : facing(player);
        return GOTO + " " + captured;
    }

    private static String standingAt(LocalPlayer player) {
        var feet = player.blockPosition();
        return feet.getX() + " " + feet.getY() + " " + feet.getZ();
    }

    private static String facing(LocalPlayer player) {
        return round(player.getXRot()) + "/" + round(player.getYRot());
    }

    private static String head(String line) {
        int space = line.indexOf(' ');
        return (space < 0 ? line : line.substring(0, space)).toLowerCase(Locale.ROOT);
    }

    private static String tail(String line) {
        int space = line.indexOf(' ');
        return space < 0 ? "" : line.substring(space + 1).trim();
    }

    private static String round(float angle) {
        return String.format(Locale.ROOT, "%.1f", angle);
    }
}