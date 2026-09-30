package dev.helm.setting;

public final class Settings {

    private static final Settings CURRENT = new Settings();

    private final MovementSettings movement = new MovementSettings();
    private final MiningSettings mining = new MiningSettings();
    private final LookSettings look = new LookSettings();
    private final PathSettings path = new PathSettings();
    private final CacheSettings cache = new CacheSettings();

    public static Settings holder() {
        return CURRENT;
    }

    public MovementSettings movement() {
        return movement;
    }

    public MiningSettings mining() {
        return mining;
    }

    public LookSettings look() {
        return look;
    }

    public PathSettings path() {
        return path;
    }

    public CacheSettings cache() {
        return cache;
    }

    public SettingSection[] sections() {
        return new SettingSection[] {movement, mining, look, path, cache};
    }

    public void restoreDefaults() {
        for (SettingSection section : sections()) {
            section.restoreDefaults();
        }
    }
}
