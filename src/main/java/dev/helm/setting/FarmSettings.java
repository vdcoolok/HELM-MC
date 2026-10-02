package dev.helm.setting;

public final class FarmSettings extends SettingSection {

    public static final String REPLANT = "farm.replantAfterHarvest";
    public static final String REPLANT_WART = "farm.replantNetherWart";
    public static final String RESCAN = "farm.rescanEveryTicks";
    public static final String MAX_TARGETS = "farm.maxTargets";
    public static final String RENDER_TARGETS = "farm.renderTargets";
    public static final String RENDER_DROPS = "farm.renderDrops";

    public FarmSettings() {
        flag(REPLANT, "Replant after harvest",
                "Plant again whatever is harvested. Turn this off to let sugar cane, bamboo "
                        + "and cactus grow back on their own, which means only the top block of "
                        + "each stalk is taken.", true);
        flag(REPLANT_WART, "Replant nether wart",
                "Plant nether wart again. Only used when replanting is on.", false);
        count(RESCAN, "Rescan every",
                "How many ticks pass between looking around for crops again. Zero looks once "
                        + "when farming starts and never again.", 5, 0, 200);
        count(MAX_TARGETS, "Max targets",
                "Most blocks to look for in one sweep.", 256, 1, 4096);
        flag(RENDER_TARGETS, "Render crops to harvest",
                "Outline the ripe crops farming is working through.", true);
        flag(RENDER_DROPS, "Render drops to collect",
                "Outline the items farming broke its crops into and still wants.", true);
    }

    public boolean replant() {
        return on(REPLANT);
    }

    public boolean replantWart() {
        return on(REPLANT_WART);
    }

    public int rescanEveryTicks() {
        return level(RESCAN);
    }

    public int maxTargets() {
        return level(MAX_TARGETS);
    }

    public boolean renderTargets() {
        return on(RENDER_TARGETS);
    }

    public boolean renderDrops() {
        return on(RENDER_DROPS);
    }
}