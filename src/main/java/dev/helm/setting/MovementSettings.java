package dev.helm.setting;

import java.util.List;

import net.minecraft.world.level.block.Block;

public final class MovementSettings extends SettingSection {

    public static final String ALLOW_BREAK = "movement.allowBreak";
    public static final String ALLOW_PLACE = "movement.allowPlace";
    public static final String PLACEMENT_PENALTY = "movement.blockPlacementPenalty";
    public static final String BREAK_PENALTY = "movement.blockBreakAdditionalPenalty";
    public static final String JUMP_PENALTY = "movement.jumpPenalty";
    public static final String WATER_SURFACE_PENALTY = "movement.walkOnWaterOnePenalty";
    public static final String MOVEMENT_TIMEOUT = "movement.movementTimeoutTicks";
    public static final String MAX_COST_INCREASE = "movement.maxCostIncrease";
    public static final String COST_LOOKAHEAD = "movement.costVerificationLookahead";
    public static final String MAX_HISTORY = "movement.maxPathHistoryLength";
    public static final String HISTORY_CUTOFF = "movement.pathHistoryCutoffAmount";
    public static final String SPLICE_PATH = "movement.splicePath";
    public static final String SPRINT_ASCENDS = "movement.sprintAscends";
    public static final String OVERSHOOT_TRAVERSE = "movement.overshootTraverse";
    public static final String PAUSE_FOR_FALLING = "movement.pauseMiningForFallingBlocks";
    public static final String JUMP_AT_BUILD_LIMIT = "movement.allowJumpAtBuildLimit";
    public static final String OVERSHOOT_DIAGONAL_DESCEND = "movement.allowOvershootDiagonalDescend";
    public static final String SPRINT_IN_WATER = "movement.sprintInWater";
    public static final String MAGMA_WALK = "movement.allowWalkOnMagmaBlocks";
    public static final String VINES_WALK = "movement.allowVines";
    public static final String BOTTOM_SLAB_WALK = "movement.allowWalkOnBottomSlab";
    public static final String ASSUME_STEP = "movement.assumeStep";
    public static final String ASSUME_SAFE_WALK = "movement.assumeSafeWalk";
    public static final String WALK_WHILE_BREAKING = "movement.walkWhileBreaking";
    public static final String ALLOW_PARKOUR = "movement.allowParkour";
    public static final String PARKOUR_PLACE = "movement.allowParkourPlace";
    public static final String PARKOUR_ASCEND = "movement.allowParkourAscend";
    public static final String DIAGONAL_ASCEND = "movement.allowDiagonalAscend";
    public static final String DIAGONAL_DESCEND = "movement.allowDiagonalDescend";
    public static final String ALLOW_DOWNWARD = "movement.allowDownward";
    public static final String ASSUME_WALK_ON_WATER = "movement.assumeWalkOnWater";
    public static final String WATER_BUCKET_FALL = "movement.allowWaterBucketFall";
    public static final String MAX_FALL_HEIGHT = "movement.maxFallHeightNoWater";
    public static final String MAX_FALL_HEIGHT_BUCKET = "movement.maxFallHeightBucket";
    public static final String AVOID_BREAKING_ENABLED = "movement.avoidBreakingMultiplierEnabled";
    public static final String AVOID_BREAKING_MULTIPLIER = "movement.avoidBreakingMultiplier";
    public static final String CONSIDER_POTIONS = "movement.considerPotionEffects";
    public static final String ASSUME_EXTERNAL_AUTOTOOL = "movement.assumeExternalAutoTool";
    public static final String ALLOW_INVENTORY = "movement.allowInventory";
    public static final String INVENTORY_TICK_GAP = "movement.ticksBetweenInventoryMoves";
    public static final String INVENTORY_ONLY_IF_STATIONARY = "movement.inventoryMoveOnlyIfStationary";
    public static final String BREAK_SPEED = "movement.blockBreakSpeed";
    public static final String PLACE_SPEED = "movement.rightClickSpeed";
    public static final String ALLOW_SPRINT = "movement.allowSprint";
    public static final String PLACEMENT_BLOCKS = "movement.placementBlocks";

    public MovementSettings() {
        flag(ALLOW_BREAK, "Allow breaking", "Blocks in the way may be mined.", true);
        flag(ALLOW_PLACE, "Allow placing",
                "Blocks may be placed to bridge gaps or step up.", true);
        amount(PLACEMENT_PENALTY, "Placement penalty",
                "Cost of placing one block, so paths prefer breaking over placing.", 20.0D, 0.0D, 1000.0D);
        amount(BREAK_PENALTY, "Break penalty",
                "Added to every block mined, so paths avoid breaking more than needed.", 2.0D, 0.0D, 1000.0D);
        amount(JUMP_PENALTY, "Jump penalty",
                "Added to every jump, because jumping costs hunger.", 2.0D, 0.0D, 1000.0D);
        amount(WATER_SURFACE_PENALTY, "Water surface penalty",
                "Added to stepping onto a water surface.", 3.0D, 0.0D, 1000.0D);
        count(MOVEMENT_TIMEOUT, "Movement timeout",
                "Ticks a single move may take before the path is abandoned.", 100, 1, 10000);
        amount(MAX_COST_INCREASE, "Max cost increase",
                "How much dearer a move may become before the path is abandoned.", 10.0D, 0.0D, 10000.0D);
        count(COST_LOOKAHEAD, "Cost lookahead",
                "How many moves ahead are checked for having become impossible.", 5, 0, 64);
        count(MAX_HISTORY, "Max history",
                "After this many executed moves the earliest ones are discarded.", 300, 10, 100000);
        count(HISTORY_CUTOFF, "History cutoff",
                "How many moves are discarded when history is trimmed.", 50, 1, 10000);
        flag(SPLICE_PATH, "Splice paths",
                "Join a newly calculated path onto the one being followed.", true);
        flag(SPRINT_ASCENDS, "Sprint ascends", "Sprint into a step up where possible.", true);
        flag(OVERSHOOT_TRAVERSE, "Overshoot steps",
                "Accept ending one or two blocks past a flat step.", true);
        flag(PAUSE_FOR_FALLING, "Pause for falling blocks",
                "Wait for sand and gravel to settle before continuing.", true);
        flag(JUMP_AT_BUILD_LIMIT, "Jump at build limit",
                "Allow parkour jumps at the very top of the world.", false);
        flag(OVERSHOOT_DIAGONAL_DESCEND, "Overshoot diagonal descend",
                "Sprint diagonally off a descending step.", true);
        flag(SPRINT_IN_WATER, "Sprint in water", "Sprint while in water.", true);
        flag(MAGMA_WALK, "Walk on magma",
                "Treat magma blocks as walkable, at a slow sneak pace.", false);
        flag(VINES_WALK, "Walk on vines", "Treat vines as a walkable surface.", false);
        flag(BOTTOM_SLAB_WALK, "Walk on bottom slabs",
                "Allow standing on bottom slabs. Turn off for more reliable paths.", true);
        flag(ASSUME_STEP, "Assume step",
                "Never jump while stepping up, assuming the game will step you.", false);
        flag(ASSUME_SAFE_WALK, "Assume safe walk",
                "Sneak while back placing, assuming the game handles edge safety.", false);
        flag(WALK_WHILE_BREAKING, "Walk while breaking",
                "Keep walking forward while breaking blocks ahead.", true);
        flag(ALLOW_PARKOUR, "Allow parkour", "Allow jumping across gaps.", true);
        flag(PARKOUR_PLACE, "Parkour place",
                "Place a block at the end of a failed parkour jump.", true);
        flag(PARKOUR_ASCEND, "Parkour ascend",
                "Sprint up a single block while parkouring.", true);
        flag(DIAGONAL_ASCEND, "Diagonal ascend",
                "Allow stepping up while moving diagonally.", true);
        flag(DIAGONAL_DESCEND, "Diagonal descend",
                "Allow stepping down while moving diagonally.", true);
        flag(ALLOW_DOWNWARD, "Allow downward",
                "Allow digging straight down to travel.", true);
        flag(ASSUME_WALK_ON_WATER, "Assume walk on water",
                "Treat water surfaces as solid ground.", false);
        flag(WATER_BUCKET_FALL, "Water bucket fall",
                "Allow a fall into water to be survivable at any height.", false);
        count(MAX_FALL_HEIGHT, "Max fall height",
                "Longest fall that is still considered, in blocks.", 3, 1, 64);
        count(MAX_FALL_HEIGHT_BUCKET, "Max bucket fall height",
                "Longest survivable fall when a water bucket is used.", 60, 1, 256);
        flag(AVOID_BREAKING_ENABLED, "Avoid listed blocks",
                "Treat blocks on the avoid list as if they were air.", false);
        amount(AVOID_BREAKING_MULTIPLIER, "Avoid multiplier",
                "How cheap breaking an avoided block is treated as being.", 0.1D, 0.0D, 1000.0D);
        flag(CONSIDER_POTIONS, "Consider potion effects",
                "Account for haste and mining fatigue when pricing breaks.", true);
        flag(ASSUME_EXTERNAL_AUTOTOOL, "Assume external auto tool",
                "Never switch tools, because another mod already does.", false);
        flag(ALLOW_INVENTORY, "Use inventory",
                "Move items between the inventory and the hotbar while walking, so tools, "
                        + "placement blocks and farm supplies are fetched from anywhere "
                        + "instead of the hotbar alone.", false);
        count(INVENTORY_TICK_GAP, "Inventory tick gap",
                "Ticks to wait between inventory moves. Zero allows one every tick.", 1, 0, 200);
        flag(INVENTORY_ONLY_IF_STATIONARY, "Inventory when stationary",
                "Stop moving before rearranging the inventory.", false);
        count(BREAK_SPEED, "Break speed", "Ticks between block break attempts.", 6, 1, 20);
        count(PLACE_SPEED, "Place speed", "Ticks between block place attempts.", 4, 1, 20);
        flag(ALLOW_SPRINT, "Allow sprint", "Allow sprinting while pathing.", true);
        blocks(PLACEMENT_BLOCKS, "Placement blocks",
                "Blocks HELM may place when a path needs one, such as bridging, "
                        + "pillaring or a failed parkour jump. Earlier blocks are preferred.",
                "minecraft:dirt, minecraft:cobblestone, minecraft:netherrack, minecraft:stone");
    }

    public boolean allowBreak() {
        return on(ALLOW_BREAK);
    }

    public boolean allowPlace() {
        return on(ALLOW_PLACE);
    }

    public double placementPenalty() {
        return rate(PLACEMENT_PENALTY);
    }

    public double breakAdditionalCost() {
        return rate(BREAK_PENALTY);
    }

    public double jumpPenalty() {
        return rate(JUMP_PENALTY);
    }

    public double waterWalkPenalty() {
        return rate(WATER_SURFACE_PENALTY);
    }

    public int movementTimeoutTicks() {
        return level(MOVEMENT_TIMEOUT);
    }

    public double maxCostIncrease() {
        return rate(MAX_COST_INCREASE);
    }

    public int costVerificationLookahead() {
        return level(COST_LOOKAHEAD);
    }

    public int maxPathHistoryLength() {
        return level(MAX_HISTORY);
    }

    public int pathHistoryCutoffAmount() {
        return level(HISTORY_CUTOFF);
    }

    public boolean splicePath() {
        return on(SPLICE_PATH);
    }

    public boolean sprintAscends() {
        return on(SPRINT_ASCENDS);
    }

    public boolean overshootTraverse() {
        return on(OVERSHOOT_TRAVERSE);
    }

    public boolean pauseMiningForFallingBlocks() {
        return on(PAUSE_FOR_FALLING);
    }

    public boolean jumpAtBuildLimit() {
        return on(JUMP_AT_BUILD_LIMIT);
    }

    public boolean overshootDiagonalDescend() {
        return on(OVERSHOOT_DIAGONAL_DESCEND);
    }

    public boolean sprintInWater() {
        return on(SPRINT_IN_WATER);
    }

    public boolean magmaWalkAllowed() {
        return on(MAGMA_WALK);
    }

    public boolean vinesWalkAllowed() {
        return on(VINES_WALK);
    }

    public boolean bottomSlabWalkAllowed() {
        return on(BOTTOM_SLAB_WALK);
    }

    public boolean assumeStep() {
        return on(ASSUME_STEP);
    }

    public boolean assumeSafeWalk() {
        return on(ASSUME_SAFE_WALK);
    }

    public boolean walkWhileBreaking() {
        return on(WALK_WHILE_BREAKING);
    }

    public boolean parkourAllowed() {
        return on(ALLOW_PARKOUR);
    }

    public boolean parkourPlaceAllowed() {
        return on(PARKOUR_PLACE);
    }

    public boolean parkourAscendAllowed() {
        return on(PARKOUR_ASCEND);
    }

    public boolean diagonalAscendAllowed() {
        return on(DIAGONAL_ASCEND);
    }

    public boolean diagonalDescendAllowed() {
        return on(DIAGONAL_DESCEND);
    }

    public boolean downwardAllowed() {
        return on(ALLOW_DOWNWARD);
    }

    public boolean assumeWalkOnWater() {
        return on(ASSUME_WALK_ON_WATER);
    }

    public boolean waterBucketFall() {
        return on(WATER_BUCKET_FALL);
    }

    public int maxFallHeightNoWater() {
        return level(MAX_FALL_HEIGHT);
    }

    public int maxFallHeightBucket() {
        return level(MAX_FALL_HEIGHT_BUCKET);
    }

    public boolean avoidBreakingEnabled() {
        return on(AVOID_BREAKING_ENABLED);
    }

    public double avoidBreakingMultiplier() {
        return rate(AVOID_BREAKING_MULTIPLIER);
    }

    public boolean considerPotionEffects() {
        return on(CONSIDER_POTIONS);
    }

    public boolean assumeExternalAutoTool() {
        return on(ASSUME_EXTERNAL_AUTOTOOL);
    }

    public boolean allowInventory() {
        return on(ALLOW_INVENTORY);
    }

    public int ticksBetweenInventoryMoves() {
        return level(INVENTORY_TICK_GAP);
    }

    public boolean inventoryMoveOnlyIfStationary() {
        return on(INVENTORY_ONLY_IF_STATIONARY);
    }

    public int blockBreakSpeed() {
        return level(BREAK_SPEED);
    }

    public int rightClickSpeed() {
        return level(PLACE_SPEED);
    }

    public boolean sprintAllowed() {
        return on(ALLOW_SPRINT);
    }

    public List<Block> placementBlocks() {
        String written = words(PLACEMENT_BLOCKS);
        if (cachedPlacementBlocks == null || !written.equals(cachedPlacementSource)) {
            cachedPlacementSource = written;
            cachedPlacementBlocks = BlockNames.ordered(written);
        }
        return cachedPlacementBlocks;
    }

    private List<Block> cachedPlacementBlocks;
    private String cachedPlacementSource;
}
