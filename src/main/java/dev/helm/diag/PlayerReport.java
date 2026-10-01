package dev.helm.diag;

import net.minecraft.client.player.LocalPlayer;

public final class PlayerReport {

    private PlayerReport() {
    }

    public static String at(LocalPlayer player) {
        return String.format("%.2f/%.2f/%.2f",
                player.getX(), player.getY(), player.getZ());
    }

    public static String feet(LocalPlayer player) {
        var block = player.blockPosition();
        return block.getX() + "," + block.getY() + "," + block.getZ();
    }

    public static String facing(LocalPlayer player) {
        return String.format("yaw %.1f pitch %.1f", player.getYRot(), player.getXRot());
    }

    public static String motion(LocalPlayer player) {
        var velocity = player.getDeltaMovement();
        return String.format("vel %.2f/%.2f/%.2f speed %.2f",
                velocity.x, velocity.y, velocity.z, velocity.length());
    }

    public static String footing(LocalPlayer player) {
        return player.onGround() ? "ground" : "air";
    }

    public static String sprinting(LocalPlayer player) {
        return player.isSprinting() ? "sprinting" : "not sprinting";
    }

    public static String stance(LocalPlayer player) {
        return player.isShiftKeyDown() ? "sneaking" : "standing";
    }

    public static String everything(LocalPlayer player) {
        return at(player) + " " + facing(player) + " " + motion(player)
                + " " + footing(player) + " " + stance(player)
                + " " + sprinting(player) + " " + food(player);
    }

    public static String food(LocalPlayer player) {
        return "food " + player.getFoodData().getFoodLevel();
    }
}
