package dev.helm.render;

public record LineColour(float red, float green, float blue, float alpha) {

    public static LineColour of(int argb) {
        float alphaChannel = ((argb >> 24) & 0xFF) / 255.0F;
        return new LineColour(
                ((argb >> 16) & 0xFF) / 255.0F,
                ((argb >> 8) & 0xFF) / 255.0F,
                (argb & 0xFF) / 255.0F,
                alphaChannel);
    }

    public LineColour withAlpha(float value) {
        return new LineColour(red, green, blue, value);
    }

    public static final LineColour PATH = of(0xFF4CE0A0);
    public static final LineColour PLACE = of(0xFF4C6CE0);
    public static final LineColour WALK_INTO = of(0xFFE0C24C);
}
