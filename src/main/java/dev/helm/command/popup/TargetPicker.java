package dev.helm.command.popup;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;

import dev.helm.follow.subject.MobTypes;

public final class TargetPicker {

    public static final int MOB_COLUMN = 0;
    public static final int PLAYER_COLUMN = 1;

    private TargetPicker() {
    }

    public static List<PopupRow> rows(String partial) {
        String wanted = wanted(partial);
        List<PopupRow> built = new ArrayList<>();
        for (MobTypes.Choice choice : MobTypes.matching(wanted)) {
            built.add(new PopupRow(choice.name(), choice.note(), MOB_COLUMN, choice.name()));
        }
        for (String name : players(wanted)) {
            built.add(new PopupRow(name, "player", PLAYER_COLUMN, name));
        }
        return built;
    }

    private static List<String> players(String wanted) {
        List<String> found = new ArrayList<>();
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return found;
        }
        for (AbstractClientPlayer player : level.players()) {
            String name = player.getName().getString();
            if (wanted.isEmpty() || name.toLowerCase(Locale.ROOT).contains(wanted)) {
                found.add(name);
            }
        }
        return found;
    }

    private static String wanted(String partial) {
        return partial == null ? "" : partial.trim().toLowerCase(Locale.ROOT);
    }
}