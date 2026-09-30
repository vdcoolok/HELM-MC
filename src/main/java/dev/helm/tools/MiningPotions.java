package dev.helm.tools;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

public final class MiningPotions {

    private MiningPotions() {
    }

    public static double amplifierOn(LivingEntity entity) {
        double speed = 1;
        MobEffectInstance haste = entity.getEffect(net.minecraft.world.effect.MobEffects.HASTE);
        if (haste != null) {
            speed *= 1 + (haste.getAmplifier() + 1) * 0.2;
        }
        MobEffectInstance fatigue = entity.getEffect(net.minecraft.world.effect.MobEffects.MINING_FATIGUE);
        if (fatigue != null) {
            speed *= fatigueFactor(fatigue.getAmplifier());
        }
        return speed;
    }

    private static double fatigueFactor(int amplifier) {
        return switch (amplifier) {
            case 0 -> 0.3;
            case 1 -> 0.09;
            case 2 -> 0.0027;
            default -> 0.00081;
        };
    }

    public static boolean affected(LivingEntity entity, net.minecraft.core.Holder<MobEffect> effect) {
        return entity.hasEffect(effect);
    }
}
