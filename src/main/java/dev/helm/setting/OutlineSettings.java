package dev.helm.setting;

public final class OutlineSettings extends SettingSection {

    public static final String ENABLED = "outline.enabled";
    public static final String BREAK_BLOCKS = "outline.blocksToBreak";
    public static final String BREAK_COLOUR = "outline.breakColour";
    public static final String CROP_COLOUR = "outline.cropColour";
    public static final String DROP_COLOUR = "outline.dropColour";
    public static final String TARGET_COLOUR = "outline.targetColour";

    public OutlineSettings() {
        flag(ENABLED, "Silhouette outlines",
                "Draw a glowing outline around the things HELM is working on.", true);
        flag(BREAK_BLOCKS, "Outline blocks to break",
                "Outline the blocks the current path is going to mine.", true);
        colour(BREAK_COLOUR, "Break outline colour",
                "Colour of the outline around blocks that will be mined.", 0xE04C4C);
        colour(CROP_COLOUR, "Crop outline colour",
                "Colour of the outline around ripe crops.", 0xB4E04C);
        colour(DROP_COLOUR, "Drop outline colour",
                "Colour of the outline around dropped items worth collecting.", 0x4CE0E0);
        colour(TARGET_COLOUR, "Target outline colour",
                "Colour of the outline around the blocks a mining job is working through.",
                0xE0C24C);
    }

    public boolean enabled() {
        return on(ENABLED);
    }

    public boolean blocksToBreak() {
        return on(BREAK_BLOCKS);
    }

    public int breakColour() {
        return tint(BREAK_COLOUR);
    }

    public int cropColour() {
        return tint(CROP_COLOUR);
    }

    public int dropColour() {
        return tint(DROP_COLOUR);
    }

    public int targetColour() {
        return tint(TARGET_COLOUR);
    }
}
