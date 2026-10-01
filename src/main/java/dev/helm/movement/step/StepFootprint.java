package dev.helm.movement.step;

public final class StepFootprint {

    private final int[] xs;
    private final int[] ys;
    private final int[] zs;

    private StepFootprint(int[] xs, int[] ys, int[] zs) {
        this.xs = xs;
        this.ys = ys;
        this.zs = zs;
    }

    public static StepFootprint of(PlanStep step) {
        Spots spots = new Spots();
        switch (step.kind()) {
            case STEP, RAISE, SINK -> endsOnly(step, spots);
            case STEP_UP -> oneBlockUp(step, spots);
            case DROP -> oneBlockDown(step, spots);
            case FALL -> longDrop(step, spots);
            case LEAN -> aroundCorner(step, spots);
            case BOUND -> acrossGap(step, spots);
        }
        return spots.freeze();
    }

    public boolean contains(int x, int y, int z) {
        for (int index = 0; index < xs.length; index++) {
            if (xs[index] == x && ys[index] == y && zs[index] == z) {
                return true;
            }
        }
        return false;
    }

    public double nearestTo(double x, double y, double z) {
        double nearest = Double.MAX_VALUE;
        for (int index = 0; index < xs.length; index++) {
            double dx = x - (xs[index] + 0.5D);
            double dy = y - (ys[index] + 0.5D);
            double dz = z - (zs[index] + 0.5D);
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (distance < nearest) {
                nearest = distance;
            }
        }
        return nearest;
    }

    private static void endsOnly(PlanStep step, Spots spots) {
        spots.add(step.fromX(), step.fromY(), step.fromZ());
        spots.add(step.toX(), step.toY(), step.toZ());
    }

    private static void oneBlockUp(PlanStep step, Spots spots) {
        int behindX = step.fromX() - step.directionX();
        int behindZ = step.fromZ() - step.directionZ();
        spots.add(step.fromX(), step.fromY(), step.fromZ());
        spots.add(step.fromX(), step.fromY() + 1, step.fromZ());
        spots.add(step.toX(), step.toY(), step.toZ());
        spots.add(behindX, step.fromY(), behindZ);
        spots.add(behindX, step.fromY() + 1, behindZ);
    }

    private static void oneBlockDown(PlanStep step, Spots spots) {
        spots.add(step.fromX(), step.fromY(), step.fromZ());
        spots.add(step.toX(), step.toY() + 1, step.toZ());
        spots.add(step.toX(), step.toY(), step.toZ());
    }

    private static void longDrop(PlanStep step, Spots spots) {
        spots.add(step.fromX(), step.fromY(), step.fromZ());
        for (int level = step.fromY(); level >= step.toY(); level--) {
            spots.add(step.toX(), level, step.toZ());
        }
    }

    private static void aroundCorner(PlanStep step, Spots spots) {
        int cornerAX = step.fromX();
        int cornerAZ = step.toZ();
        int cornerBX = step.toX();
        int cornerBZ = step.fromZ();
        spots.add(step.fromX(), step.fromY(), step.fromZ());
        if (step.toY() < step.fromY()) {
            spots.add(step.toX(), step.toY() + 1, step.toZ());
            spots.add(step.toX(), step.toY(), step.toZ());
            spots.add(cornerAX, step.fromY() - 1, cornerAZ);
            spots.add(cornerBX, step.fromY() - 1, cornerBZ);
        } else if (step.toY() > step.fromY()) {
            spots.add(step.fromX(), step.fromY() + 1, step.fromZ());
            spots.add(step.toX(), step.toY(), step.toZ());
            spots.add(cornerAX, step.fromY() + 1, cornerAZ);
            spots.add(cornerBX, step.fromY() + 1, cornerBZ);
        } else {
            spots.add(step.toX(), step.toY(), step.toZ());
        }
        spots.add(cornerAX, step.fromY(), cornerAZ);
        spots.add(cornerBX, step.fromY(), cornerBZ);
    }

    private static void acrossGap(PlanStep step, Spots spots) {
        int reach = Math.abs(step.toX() - step.fromX()) + Math.abs(step.toZ() - step.fromZ());
        for (int along = 0; along <= reach; along++) {
            int x = step.fromX() + step.directionX() * along;
            int z = step.fromZ() + step.directionZ() * along;
            spots.add(x, step.fromY(), z);
            spots.add(x, step.fromY() + 1, z);
        }
    }

    private static final class Spots {

        private int[] xs = new int[8];
        private int[] ys = new int[8];
        private int[] zs = new int[8];
        private int count;

        void add(int x, int y, int z) {
            if (count == xs.length) {
                xs = grow(xs);
                ys = grow(ys);
                zs = grow(zs);
            }
            xs[count] = x;
            ys[count] = y;
            zs[count] = z;
            count++;
        }

        StepFootprint freeze() {
            int[] x = new int[count];
            int[] y = new int[count];
            int[] z = new int[count];
            System.arraycopy(xs, 0, x, 0, count);
            System.arraycopy(ys, 0, y, 0, count);
            System.arraycopy(zs, 0, z, 0, count);
            return new StepFootprint(x, y, z);
        }

        private static int[] grow(int[] values) {
            int[] larger = new int[values.length * 2];
            System.arraycopy(values, 0, larger, 0, values.length);
            return larger;
        }
    }
}
