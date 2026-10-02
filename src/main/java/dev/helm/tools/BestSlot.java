package dev.helm.tools;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import dev.helm.inventory.InventorySlots;
import dev.helm.setting.MiningSettings;

public final class BestSlot {

    private BestSlot() {
    }

    public static int forBlock(Block block, boolean preferSilkTouch, boolean forPathCost,
                               InventoryView inventory, MiningSettings settings) {
        if (!settings.autoTool() && forPathCost) {
            return inventory.selectedSlot();
        }
        int best = 0;
        double highestSpeed = Double.NEGATIVE_INFINITY;
        int lowestQuality = Integer.MIN_VALUE;
        boolean bestSilkTouch = false;
        for (int slot = 0; slot < InventorySlots.HOTBAR; slot++) {
            var stack = inventory.slot(slot);
            if (!allowed(stack, settings)) {
                continue;
            }
            double speed = MiningSpeed.perTick(stack, block.defaultBlockState());
            boolean silkTouch = SilkTouch.on(stack);
            if (speed > highestSpeed) {
                highestSpeed = speed;
                best = slot;
                lowestQuality = ToolMaterials.qualityOf(stack);
                bestSilkTouch = silkTouch;
            } else if (speed == highestSpeed) {
                int quality = ToolMaterials.qualityOf(stack);
                if ((quality < lowestQuality && (silkTouch || !bestSilkTouch))
                        || (preferSilkTouch && !bestSilkTouch && silkTouch)) {
                    highestSpeed = speed;
                    best = slot;
                    lowestQuality = quality;
                    bestSilkTouch = silkTouch;
                }
            }
        }
        return best;
    }

    public static double speedIn(ItemStack stack, BlockState state) {
        return stack.isEmpty() ? 0.0D : MiningSpeed.perTick(stack, state);
    }

    public static int fastestPacked(BlockState state, double beat, InventoryView inventory,
                                   MiningSettings settings) {
        int best = -1;
        double fastest = beat;
        for (int slot = InventorySlots.FIRST_PACKED; slot < InventorySlots.CARRIED; slot++) {
            ItemStack stack = inventory.slot(slot);
            if (stack.isEmpty() || !allowed(stack, settings)) {
                continue;
            }
            double speed = MiningSpeed.perTick(stack, state);
            if (speed > fastest) {
                fastest = speed;
                best = slot;
            }
        }
        return best;
    }

    public static int bestAgainst(Block block, boolean preferSilkTouch, InventoryView inventory,
                                  MiningSettings settings) {
        BlockState state = block.defaultBlockState();
        int best = -1;
        double highestSpeed = 0.0D;
        for (int slot = 0; slot < InventorySlots.CARRIED; slot++) {
            ItemStack stack = inventory.slot(slot);
            if (!stack.isEmpty() && !ToolSet.isTool(stack)) {
                continue;
            }
            if (!allowed(stack, settings)) {
                continue;
            }
            double speed = MiningSpeed.perTick(stack, state);
            if (speed <= 0.0D) {
                continue;
            }
            if (speed > highestSpeed || (speed == highestSpeed && preferSilkTouch
                    && SilkTouch.on(stack))) {
                highestSpeed = speed;
                best = slot;
            }
        }
        return best;
    }

    private static boolean allowed(ItemStack stack, MiningSettings settings) {
        if (!settings.swordToMine() && stack.is(ItemTags.SWORDS)) {
            return false;
        }
        if (settings.itemSaver() && stack.getMaxDamage() > 1
                && stack.getDamageValue() + settings.itemSaverThreshold() >= stack.getMaxDamage()) {
            return false;
        }
        return true;
    }
}