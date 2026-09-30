package dev.helm.tools;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import dev.helm.setting.MiningSettings;
import dev.helm.setting.MovementSettings;

public final class MinedToolStrength implements BreakStrength {

    private final Map<Block, Double> cache = new HashMap<>();
    private final PlayerInventoryView inventory;
    private final MiningSettings mining;
    private final MovementSettings movement;

    public MinedToolStrength(PlayerInventoryView inventory, MiningSettings mining,
                              MovementSettings movement) {
        this.inventory = inventory;
        this.mining = mining;
        this.movement = movement;
    }

    @Override
    public double against(Block block) {
        return cache.computeIfAbsent(block, this::measure);
    }

    @Override
    public void invalidate() {
        cache.clear();
    }

    private double measure(Block block) {
        ItemStack stack = inventory.slot(BestSlot.forBlock(block, mining.preferSilkTouch(), true,
                inventory, mining));
        double speed = MiningSpeed.perTick(stack, block.defaultBlockState());
        if (mining.considerPotionEffects() && inventory.player() != null) {
            speed *= MiningPotions.amplifierOn(inventory.player());
        }
        return speed * avoidance(block);
    }

    private double avoidance(Block block) {
        return movement.avoidBreakingEnabled() && BlockAvoidList.contains(block)
                ? movement.avoidBreakingMultiplier()
                : 1.0D;
    }
}
