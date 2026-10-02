package dev.helm.inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.concurrent.ThreadLocalRandom;

import net.minecraft.client.player.LocalPlayer;

import dev.helm.world.PlayerInventory;

public final class HotbarSpace {

    private static final int FIRST_SPARE = 1;
    private static final int LAST_SPARE = 7;

    private HotbarSpace() {
    }

    public static OptionalInt spare(LocalPlayer player) {
        List<Integer> empties = new ArrayList<>();
        List<Integer> occupied = new ArrayList<>();
        PlayerInventory inventory = new PlayerInventory(player);
        for (int slot = FIRST_SPARE; slot <= LAST_SPARE; slot++) {
            if (inventory.slot(slot).isEmpty()) {
                empties.add(slot);
            } else {
                occupied.add(slot);
            }
        }
        return choose(empties.isEmpty() ? occupied : empties);
    }

    private static OptionalInt choose(List<Integer> candidates) {
        if (candidates.isEmpty()) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(candidates.get(ThreadLocalRandom.current()
                .nextInt(candidates.size())));
    }
}