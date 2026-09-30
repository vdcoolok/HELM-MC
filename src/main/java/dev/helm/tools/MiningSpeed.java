package dev.helm.tools;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.enchantment.effects.EnchantmentAttributeEffect;
import net.minecraft.world.level.block.state.BlockState;

public final class MiningSpeed {

    private static final float WRONG_TOOL_DIVISOR = 30.0F;
    private static final float CORRECT_TOOL_DIVISOR = 100.0F;

    private MiningSpeed() {
    }

    public static double perTick(ItemStack item, BlockState state) {
        float hardness;
        try {
            hardness = state.getDestroySpeed(null, null);
        } catch (NullPointerException unavailable) {
            return -1;
        }
        if (hardness < 0) {
            return -1;
        }
        float speed = item.getDestroySpeed(state);
        if (speed > 1) {
            speed += efficiencyBonus(item.getEnchantments());
        }
        speed /= hardness;
        if (!state.requiresCorrectToolForDrops()
                || (!item.isEmpty() && item.isCorrectToolForDrops(state))) {
            return speed / WRONG_TOOL_DIVISOR;
        }
        return speed / CORRECT_TOOL_DIVISOR;
    }

    private static float efficiencyBonus(ItemEnchantments enchantments) {
        for (Holder<Enchantment> enchantment : enchantments.keySet()) {
            for (EnchantmentAttributeEffect effect
                    : enchantment.value().getEffects(EnchantmentEffectComponents.ATTRIBUTES)) {
                if (effect.attribute().is(Attributes.MINING_EFFICIENCY.unwrapKey().get())) {
                    return effect.amount().calculate(enchantments.getLevel(enchantment));
                }
            }
        }
        return 0.0F;
    }
}
