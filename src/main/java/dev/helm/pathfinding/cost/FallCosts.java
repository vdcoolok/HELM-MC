package dev.helm.pathfinding.cost;

public final class FallCosts {

    private static final double GRAVITY = 3.92;
    private static final double DRAG = 0.98;
    private static final int TABLE_LIMIT = 4097;

    private static final double[] BY_DISTANCE = buildTable();

    private FallCosts() {
    }

    public static double velocity(int ticks) {
        return (Math.pow(DRAG, ticks) - 1) * -GRAVITY;
    }

    public static double ticksToFall(double distance) {
        if (distance == 0) {
            return 0;
        }
        double remaining = distance;
        int ticks = 0;
        while (true) {
            double fall = velocity(ticks);
            if (remaining <= fall) {
                return ticks + remaining / fall;
            }
            remaining -= fall;
            ticks++;
        }
    }

    public static double forDistance(double distance) {
        if (distance <= 0) {
            return 0;
        }
        if (distance < TABLE_LIMIT) {
            return BY_DISTANCE[(int) distance];
        }
        return ticksToFall(distance);
    }

    public static double oneAndAQuarter() {
        return ticksToFall(1.25);
    }

    public static double quarter() {
        return ticksToFall(0.25);
    }

    public static double jumpOneBlock() {
        return oneAndAQuarter() - quarter();
    }

    private static double[] buildTable() {
        double[] table = new double[TABLE_LIMIT];
        for (int index = 0; index < TABLE_LIMIT; index++) {
            table[index] = ticksToFall(index);
        }
        return table;
    }
}