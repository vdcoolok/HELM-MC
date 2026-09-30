package dev.helm.tools;

public final class BestSlot {

    private BestSlot() {
    }

    public static int forBlock(net.minecraft.world.level.block.Block block, boolean preferSilkTouch,
                               boolean forPathCost, InventoryView inventory,
                               dev.helm.setting.MiningSettings settings) {
        if (!settings.autoTool() && forPathCost) {
            return inventory.selectedSlot();
        }
        int best = 0;
        double highestSpeed = Double.NEGATIVE_INFINITY;
        int lowestQuality = Integer.MIN_VALUE;
        boolean bestSilkTouch = false;
        for (int slot = 0; slot < 9; slot++) {
            var stack = inventory.slot(slot);
            if (!settings.swordToMine() && stack.is(net.minecraft.tags.ItemTags.SWORDS)) {
                continue;
            }
            if (settings.itemSaver() && stack.getMaxDamage() > 1
                    && stack.getDamageValue() + settings.itemSaverThreshold() >= stack.getMaxDamage()) {
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
}
