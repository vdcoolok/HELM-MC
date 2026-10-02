package dev.helm.world.read;

import java.util.ArrayList;
import java.util.List;

final class Ring {

    private Ring() {
    }

    static List<int[]> at(int squaredRadius) {
        int reach = (int) Math.sqrt(squaredRadius);
        List<int[]> offsets = new ArrayList<>();
        for (int x = -reach; x <= reach; x++) {
            for (int z = -reach; z <= reach; z++) {
                if (x * x + z * z == squaredRadius) {
                    offsets.add(new int[]{x, z});
                }
            }
        }
        return offsets;
    }
}