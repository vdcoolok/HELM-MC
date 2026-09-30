package dev.helm.setting;

import java.util.Set;

import net.minecraft.world.level.block.Block;

public final class MiningSettings extends SettingSection {

    public static final String AUTO_TOOL = "mining.autoTool";
    public static final String PREFER_SILK_TOUCH = "mining.preferSilkTouch";
    public static final String SWORD_TO_MINE = "mining.useSwordToMine";
    public static final String ITEM_SAVER = "mining.itemSaver";
    public static final String ITEM_SAVER_THRESHOLD = "mining.itemSaverThreshold";
    public static final String AVOID_BREAKING = "mining.avoidBreaking";
    public static final String CONSIDER_POTIONS = "mining.considerPotionEffects";

    public MiningSettings() {
        flag(AUTO_TOOL, "Auto tool",
                "Switch to the fastest tool for every block mined.", true);
        flag(PREFER_SILK_TOUCH, "Prefer silk touch",
                "Pick a silk touch tool when it is no slower.", false);
        flag(SWORD_TO_MINE, "Use sword to mine",
                "Allow swords to be chosen as a mining tool.", true);
        flag(ITEM_SAVER, "Item saver",
                "Stop using a tool once it is nearly broken.", false);
        count(ITEM_SAVER_THRESHOLD, "Item saver threshold",
                "Durability left on a tool when the item saver stops using it.", 10, 1, 1000);
        text(AVOID_BREAKING, "Blocks to avoid breaking",
                "Comma separated block names that are treated as air.", "");
        flag(CONSIDER_POTIONS, "Consider potion effects",
                "Account for haste and mining fatigue when pricing breaks.", true);
    }

    public boolean autoTool() {
        return on(AUTO_TOOL);
    }

    public boolean preferSilkTouch() {
        return on(PREFER_SILK_TOUCH);
    }

    public boolean swordToMine() {
        return on(SWORD_TO_MINE);
    }

    public boolean itemSaver() {
        return on(ITEM_SAVER);
    }

    public int itemSaverThreshold() {
        return level(ITEM_SAVER_THRESHOLD);
    }

    public String avoidBreakingList() {
        return words(AVOID_BREAKING);
    }

    public Set<Block> blocksToAvoidBreaking() {
        return BlockNames.parse(avoidBreakingList());
    }

    public boolean considerPotionEffects() {
        return on(CONSIDER_POTIONS);
    }
}
