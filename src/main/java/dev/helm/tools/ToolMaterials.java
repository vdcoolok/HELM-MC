package dev.helm.tools;

import java.util.List;

import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class ToolMaterials {

    private static final List<TagKey<Item>> BY_QUALITY = List.of(
            ItemTags.WOODEN_TOOL_MATERIALS,
            ItemTags.STONE_TOOL_MATERIALS,
            ItemTags.COPPER_TOOL_MATERIALS,
            ItemTags.IRON_TOOL_MATERIALS,
            ItemTags.GOLD_TOOL_MATERIALS,
            ItemTags.DIAMOND_TOOL_MATERIALS,
            ItemTags.NETHERITE_TOOL_MATERIALS);

    private ToolMaterials() {
    }

    public static int qualityOf(ItemStack stack) {
        for (int quality = 0; quality < BY_QUALITY.size(); quality++) {
            if (stack.is(BY_QUALITY.get(quality))) {
                return quality;
            }
        }
        return -1;
    }
}
