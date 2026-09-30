package dev.helm.tools;

import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public final class SilkTouch {

    private SilkTouch() {
    }

    public static boolean on(ItemStack stack) {
        ItemEnchantments enchantments = stack.getEnchantments();
        for (Holder<Enchantment> enchantment : enchantments.keySet()) {
            if (enchantment.is(Enchantments.SILK_TOUCH) && enchantments.getLevel(enchantment) > 0) {
                return true;
            }
        }
        return false;
    }
}
