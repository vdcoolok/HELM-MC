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
    public static final String BREAK_ALLOWED_ANYWAY = "mining.breakAllowedAnyway";
    public static final String MAX_TARGETS = "mining.maxTargets";
    public static final String LOWEST_LEVEL = "mining.lowestLevel";
    public static final String HIGHEST_LEVEL = "mining.highestLevel";
    public static final String ONLY_EXPOSED = "mining.onlyExposed";
    public static final String EXPOSED_RADIUS = "mining.exposedRadius";
    public static final String SIGHT_ONLY = "mining.sightOnly";
    public static final String SIGHT_DIAGONALS = "mining.sightDiagonals";
    public static final String STRIP_LEVEL = "mining.stripLevel";
    public static final String EXPLORE_WHEN_UNKNOWN = "mining.exploreWhenUnknown";
    public static final String SKIP_UNREACHABLE = "mining.skipUnreachable";
    public static final String RESCAN_EVERY = "mining.rescanEveryTicks";
    public static final String SCAN_WHEN_CACHE_THIN = "mining.scanWhenCacheThin";
    public static final String FOLLOW_DROPS = "mining.followDroppedItems";
    public static final String DROP_WAIT_MILLIS = "mining.dropWaitMillis";
    public static final String DIG_INTO_VEIN = "mining.digIntoVein";
    public static final String DIG_THROUGH_AIR = "mining.digThroughAir";
    public static final String BREAK_OVERHEAD = "mining.breakOverhead";
    public static final String PILLAR_TO_REACH = "mining.pillarToReach";
    public static final String STOP_ROUTE_WHEN_MINED = "mining.stopRouteWhenMined";
    public static final String REPACK_RADIUS = "mining.repackRadius";
    public static final String SCAN_RADIUS = "mining.scanRadius";
    public static final String SCAN_LEVEL_WINDOW = "mining.scanLevelWindow";
    public static final String CACHE_SCAN_RADIUS = "mining.cacheScanRadius";
    public static final String CACHE_SCAN_LIMIT = "mining.cacheScanLimit";
    public static final String RENDER_TARGETS = "mining.renderTargets";

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
        blocks(BREAK_ALLOWED_ANYWAY, "Blocks allowed to be mined anyway",
                "Blocks that $mine will still mine while breaking in general is turned off.", "");
        count(MAX_TARGETS, "Max targets",
                "Most places to remember at once. Only the closest are kept once this is "
                        + "reached, so lower numbers find nearer blocks first.", 64, 1, 4096);
        count(LOWEST_LEVEL, "Lowest level",
                "Lowest level a target may be on. Zero uses the bottom of the world instead, "
                        + "so this works the same in every dimension.", 0, 0, 4096);
        count(HIGHEST_LEVEL, "Highest level",
                "Highest level a target may be on.", 2031, -4096, 4096);
        flag(ONLY_EXPOSED, "Only exposed targets",
                "Only mine targets with air or liquid touching them, which reads much less "
                        + "like looking through stone on servers that shuffle ores around.", false);
        count(EXPOSED_RADIUS, "Exposed radius",
                "How far around a target to look for air or liquid when only exposed targets "
                        + "is on. Higher values are much slower to check.", 1, 0, 8);
        flag(SIGHT_ONLY, "Sight only",
                "Never act on a target that cannot be seen from where you stand, and always "
                        + "look for more instead. Turn this on to avoid reading like you can "
                        + "see through stone.", false);
        flag(SIGHT_DIAGONALS, "Sight diagonals",
                "While sight only is on, also accept a target that only touches a target you "
                        + "can already see.", false);
        count(STRIP_LEVEL, "Strip level",
                "Level to hold while exploring for targets that are not known yet.", -59, -4096, 4096);
        flag(EXPLORE_WHEN_UNKNOWN, "Explore when unknown",
                "Walk away from where you started when no target is known, so new chunks load "
                        + "and something turns up.", true);
        flag(SKIP_UNREACHABLE, "Skip unreachable",
                "When no route can be found, mark the closest target as unreachable and work "
                        + "on the next one instead of giving up.", true);
        count(RESCAN_EVERY, "Rescan every",
                "Ticks between looking for targets again. Zero looks once when mining starts "
                        + "and never again.", 5, 0, 200);
        flag(SCAN_WHEN_CACHE_THIN, "Scan when the cache is thin",
                "Also read the loaded world when the remembered chunks hold fewer targets "
                        + "than the limit above. Costs much more time.", false);
        flag(FOLLOW_DROPS, "Follow dropped items",
                "Walk over and pick up dropped items that came out of the targets being mined.",
                true);
        count(DROP_WAIT_MILLIS, "Drop wait",
                "Milliseconds to wait after breaking a target before moving on, in case it "
                        + "drops something worth collecting.", 250, 0, 10000);
        flag(DIG_INTO_VEIN, "Dig into the vein",
                "Path into the block behind a target when the vein runs through it, so one "
                        + "break can take two or three.", true);
        flag(DIG_THROUGH_AIR, "Dig through air",
                "Only used while digging into the vein is on. Treats air next to a target as "
                        + "part of the vein, so a single loose block does not stop the dig.", true);
        flag(BREAK_OVERHEAD, "Break what is overhead",
                "When a target sits straight above you, stand still and break it instead of "
                        + "walking somewhere first. This is what takes a tree down.", true);
        flag(PILLAR_TO_REACH, "Pillar to reach",
                "When a target is straight above you but too high to break, place a block under "
                        + "yourself and step up, then try again, until the rest of the vein is "
                        + "out of reach no more. Costs one block for every level, and needs "
                        + "movement allowing placing and breaking.", true);
        flag(STOP_ROUTE_WHEN_MINED, "Stop the route when mined",
                "Stop walking as soon as the block a route was heading for is gone, rather "
                        + "than finishing a route that no longer leads anywhere.", true);
        count(REPACK_RADIUS, "Repack radius",
                "Chunks around you remembered before mining starts, so routes can cross ground "
                        + "that is no longer loaded.", 40, 0, 64);
        count(SCAN_RADIUS, "Scan radius",
                "Chunks around you read when looking for targets that the remembered chunks "
                        + "do not hold.", 32, 1, 64);
        count(SCAN_LEVEL_WINDOW, "Scan level window",
                "How far from your own level the scan keeps looking before it decides a chunk "
                        + "has nothing for you.", 10, 0, 512);
        count(CACHE_SCAN_RADIUS, "Cache scan radius",
                "How far around you the remembered chunks are searched for targets.", 2, 0, 8);
        count(CACHE_SCAN_LIMIT, "Cache scan limit",
                "After finding this many targets in the remembered chunks, the search stops "
                        + "widening and the loaded world is read instead.", 10, 1, 4096);
        flag(RENDER_TARGETS, "Render targets",
                "Outline every block being mined, not just the route's own breaks.", true);
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

    public Set<Block> blocksAllowedToMine() {
        return BlockNames.parse(words(BREAK_ALLOWED_ANYWAY));
    }

    public boolean considerPotionEffects() {
        return on(CONSIDER_POTIONS);
    }

    public int maxTargets() {
        return level(MAX_TARGETS);
    }

    public int lowestLevel() {
        return level(LOWEST_LEVEL);
    }

    public int highestLevel() {
        return level(HIGHEST_LEVEL);
    }

    public boolean onlyExposed() {
        return on(ONLY_EXPOSED);
    }

    public int exposedRadius() {
        return level(EXPOSED_RADIUS);
    }

    public boolean sightOnly() {
        return on(SIGHT_ONLY);
    }

    public boolean sightDiagonals() {
        return on(SIGHT_DIAGONALS);
    }

    public int stripLevel() {
        return level(STRIP_LEVEL);
    }

    public boolean exploreWhenUnknown() {
        return on(EXPLORE_WHEN_UNKNOWN);
    }

    public boolean skipUnreachable() {
        return on(SKIP_UNREACHABLE);
    }

    public int rescanEveryTicks() {
        return level(RESCAN_EVERY);
    }

    public boolean scanWhenCacheThin() {
        return on(SCAN_WHEN_CACHE_THIN);
    }

    public boolean followDroppedItems() {
        return on(FOLLOW_DROPS);
    }

    public int dropWaitMillis() {
        return level(DROP_WAIT_MILLIS);
    }

    public boolean digIntoVein() {
        return on(DIG_INTO_VEIN);
    }

    public boolean digThroughAir() {
        return on(DIG_THROUGH_AIR);
    }

    public boolean breakOverhead() {
        return on(BREAK_OVERHEAD);
    }

    public boolean pillarToReach() {
        return on(PILLAR_TO_REACH);
    }

    public boolean stopRouteWhenMined() {
        return on(STOP_ROUTE_WHEN_MINED);
    }

    public int repackRadius() {
        return level(REPACK_RADIUS);
    }

    public int scanRadius() {
        return level(SCAN_RADIUS);
    }

    public int scanLevelWindow() {
        return level(SCAN_LEVEL_WINDOW);
    }

    public int cacheScanRadius() {
        return level(CACHE_SCAN_RADIUS);
    }

    public int cacheScanLimit() {
        return level(CACHE_SCAN_LIMIT);
    }

    public boolean renderTargets() {
        return on(RENDER_TARGETS);
    }
}