package dev.helm.world.cache;

public enum TerrainKind {

    AIR(0b00),
    WATER(0b01),
    AVOID(0b10),
    SOLID(0b11);

    private static final TerrainKind[] TABLE = values();

    private final int code;

    TerrainKind(int code) {
        this.code = code;
    }

    public static TerrainKind of(boolean high, boolean low) {
        return TABLE[(high ? 0b10 : 0) | (low ? 0b01 : 0)];
    }

    public boolean high() {
        return (code & 0b10) != 0;
    }

    public boolean low() {
        return (code & 0b01) != 0;
    }

    public boolean occupied() {
        return this != AIR;
    }
}
