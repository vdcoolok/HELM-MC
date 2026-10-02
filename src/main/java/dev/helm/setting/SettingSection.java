package dev.helm.setting;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class SettingSection {

    private final Map<String, Setting<?>> byKey = new LinkedHashMap<>();
    private final List<Setting<?>> declared = new ArrayList<>();

    protected final Setting<Boolean> flag(String key, String label, String detail, boolean initial) {
        return register(Setting.flag(key, label, detail, initial));
    }

    protected final Setting<Integer> count(String key, String label, String detail, int initial,
                                          int minimum, int maximum) {
        return register(Setting.count(key, label, detail, initial, minimum, maximum));
    }

    protected final Setting<Double> amount(String key, String label, String detail, double initial,
                                          double minimum, double maximum) {
        return register(Setting.amount(key, label, detail, initial, minimum, maximum));
    }

    protected final Setting<String> text(String key, String label, String detail, String initial) {
        return register(Setting.text(key, label, detail, initial));
    }

    protected final Setting<String> blocks(String key, String label, String detail, String initial) {
        return register(Setting.blocks(key, label, detail, initial));
    }

    @SuppressWarnings("unchecked")
    protected final void apply(String key, String value) {
        Setting<?> setting = byKey.get(key);
        if (setting == null) {
            return;
        }
        switch (setting.kind()) {
            case BOOLEAN -> setting.accept(Boolean.valueOf(value));
            case WHOLE -> setting.accept(parseWhole(value, setting.initial()));
            case DECIMAL -> setting.accept(parseDecimal(value, setting.initial()));
            case TEXT, BLOCKS -> setting.accept(value);
        }
    }

    private static int parseWhole(String value, Object fallback) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException malformed) {
            return (Integer) fallback;
        }
    }

    private static double parseDecimal(String value, Object fallback) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException malformed) {
            return (Double) fallback;
        }
    }

    private <T> Setting<T> register(Setting<T> setting) {
        if (byKey.putIfAbsent(setting.key(), setting) != null) {
            throw new IllegalStateException("Duplicate setting key " + setting.key());
        }
        declared.add(setting);
        return setting;
    }

    public final List<Setting<?>> declared() {
        return List.copyOf(declared);
    }

    public final Setting<?> find(String key) {
        return byKey.get(key);
    }

    public final void restoreDefaults() {
        declared.forEach(Setting::restore);
    }

    protected final boolean on(String key) {
        return require(key, Boolean.class);
    }

    protected final int level(String key) {
        return require(key, Integer.class);
    }

    protected final double rate(String key) {
        return require(key, Double.class);
    }

    protected final String words(String key) {
        return require(key, String.class);
    }

    private <T> T require(String key, Class<T> type) {
        Setting<?> setting = byKey.get(key);
        if (setting == null) {
            throw new IllegalArgumentException("Unknown setting " + key);
        }
        return type.cast(setting.value());
    }
}
