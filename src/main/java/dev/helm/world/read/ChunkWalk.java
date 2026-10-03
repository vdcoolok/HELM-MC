package dev.helm.world.read;

final class ChunkWalk {

    private final int originX;
    private final int originZ;
    private final int outermost;
    private int ring;
    private int walked;
    private int chunkX;
    private int chunkZ;

    ChunkWalk(int originX, int originZ, int radius) {
        this.originX = originX;
        this.originZ = originZ;
        this.outermost = Math.max(radius, 0);
        this.chunkX = originX;
        this.chunkZ = originZ;
    }

    boolean next() {
        if (ring > outermost) {
            return false;
        }
        if (walked == width()) {
            ring++;
            walked = 0;
            if (ring > outermost) {
                return false;
            }
        }
        place();
        walked++;
        return true;
    }

    int chunkX() {
        return chunkX;
    }

    int chunkZ() {
        return chunkZ;
    }

    private int width() {
        return ring == 0 ? 1 : 8 * ring;
    }

    private void place() {
        if (ring == 0) {
            chunkX = originX;
            chunkZ = originZ;
            return;
        }
        int side = 2 * ring;
        int edge = walked / side;
        int along = walked % side;
        switch (edge) {
            case 0 -> {
                chunkX = originX - ring + along;
                chunkZ = originZ - ring;
            }
            case 1 -> {
                chunkX = originX + ring;
                chunkZ = originZ - ring + along;
            }
            case 2 -> {
                chunkX = originX + ring - along;
                chunkZ = originZ + ring;
            }
            default -> {
                chunkX = originX - ring;
                chunkZ = originZ + ring - along;
            }
        }
    }
}
