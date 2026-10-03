package dev.helm.follow.subject;

public final class PlayerRoster {

    private PlayerRoster() {
    }

    public static boolean holds(net.minecraft.client.multiplayer.ClientLevel level, String wanted) {
        if (level == null || wanted == null || wanted.isBlank()) {
            return false;
        }
        for (net.minecraft.client.player.AbstractClientPlayer player : level.players()) {
            if (player.getName().getString().equalsIgnoreCase(wanted.trim())) {
                return true;
            }
        }
        return false;
    }
}