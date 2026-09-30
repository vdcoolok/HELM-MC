package dev.helm.movement;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

public final class FrostWalker {

    private FrostWalker() {
    }

    public static boolean wornBy(net.minecraft.world.entity.LivingEntity entity) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            for (Holder<Enchantment> enchantment
                    : entity.getItemBySlot(slot).getEnchantments().keySet()) {
                if (enchantment.is(Enchantments.FROST_WALKER)) {
                    return true;
                }
            }
        }
        return false;
    }
}
