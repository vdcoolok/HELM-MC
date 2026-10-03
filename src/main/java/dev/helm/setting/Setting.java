package dev.helm.setting;

import java.util.Locale;

public final class Setting<T> {

    private final String key;
    private final String label;
    private final String detail;
    private final T initial;
    private final SettingKind kind;
    private final T minimum;
    private final T maximum;

    private T value;

    public Setting(String key, String label, String detail, T initial, SettingKind kind,
                   T minimum, T maximum) {
        this.key = key;
        this.label = label;
        this.detail = detail;
        this.initial = initial;
        this.kind = kind;
        this.minimum = minimum;
        this.maximum = maximum;
        this.value = initial;
    }

    public static Setting<Boolean> flag(String key, String label, String detail, boolean initial) {
        return new Setting<>(key, label, detail, initial, SettingKind.BOOLEAN, null, null);
    }

    public static Setting<Integer> count(String key, String label, String detail, int initial,
                                         int minimum, int maximum) {
        return new Setting<>(key, label, detail, initial, SettingKind.WHOLE, minimum, maximum);
    }

    public static Setting<Double> amount(String key, String label, String detail, double initial,
                                         double minimum, double maximum) {
        return new Setting<>(key, label, detail, initial, SettingKind.DECIMAL, minimum, maximum);
    }

    public static Setting<String> text(String key, String label, String detail, String initial) {
        return new Setting<>(key, label, detail, initial, SettingKind.TEXT, null, null);
    }

    public static Setting<String> blocks(String key, String label, String detail, String initial) {
        return new Setting<>(key, label, detail, initial, SettingKind.BLOCKS, null, null);
    }

    public static Setting<Integer> colour(String key, String label, String detail, int initial) {
        return new Setting<>(key, label, detail, initial, SettingKind.COLOUR, 0, 0xFFFFFF);
    }

    public String key() {
        return key;
    }

    public String label() {
        return label;
    }

    public String detail() {
        return detail;
    }

    public SettingKind kind() {
        return kind;
    }

    public T initial() {
        return initial;
    }

    public T value() {
        return value;
    }

    public String written() {
        if (kind == SettingKind.COLOUR && value instanceof Integer rgb) {
            return String.format(Locale.ROOT, "#%06X", rgb);
        }
        return String.valueOf(value);
    }

    @SuppressWarnings("unchecked")
    public void accept(Object candidate) {
        if (candidate == null) {
            return;
        }
        this.value = clamp((T) candidate);
    }

    public void restore() {
        this.value = initial;
    }

    @SuppressWarnings("unchecked")
    private T clamp(T candidate) {
        if (minimum instanceof Double low && candidate instanceof Double value) {
            return (T) Double.valueOf(Math.max(low, Math.min((Double) maximum, value)));
        }
        if (minimum instanceof Integer low && candidate instanceof Integer value) {
            return (T) Integer.valueOf(Math.max(low, Math.min((Integer) maximum, value)));
        }
        return candidate;
    }
}
