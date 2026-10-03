package dev.helm.outline;

import net.minecraft.util.ARGB;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

import dev.helm.drops.DropCache;
import dev.helm.setting.OutlineSettings;
import dev.helm.setting.Settings;

public final class ItemSilhouette {

    private ItemSilhouette() {
    }

    public static int colourOf(Entity entity) {
        if (!(entity instanceof ItemEntity dropped)) {
            return 0;
        }
        OutlineSettings settings = Settings.holder().outline();
        if (!settings.enabled() || !Settings.holder().farm().renderItems()) {
            return 0;
        }
        ItemStack stack = dropped.getItem();
        if (!DropCache.instance().wanted(stack)) {
            return 0;
        }
        return ARGB.opaque(settings.dropColour());
    }
}
