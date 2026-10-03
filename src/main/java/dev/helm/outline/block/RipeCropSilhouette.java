package dev.helm.outline.block;

import java.util.Collection;
import java.util.List;

import net.minecraft.core.BlockPos;

import dev.helm.farm.FarmTask;
import dev.helm.setting.OutlineSettings;
import dev.helm.setting.Settings;

public final class RipeCropSilhouette implements BlockSilhouetteFeed {

    private static final RipeCropSilhouette INSTANCE = new RipeCropSilhouette();

    private RipeCropSilhouette() {
    }

    public static RipeCropSilhouette instance() {
        return INSTANCE;
    }

    @Override
    public Collection<BlockPos> watch() {
        if (!Settings.holder().farm().renderCrops()) {
            return List.of();
        }
        return FarmTask.instance().harvestable();
    }

    @Override
    public int colour() {
        OutlineSettings settings = Settings.holder().outline();
        return Settings.holder().farm().renderCrops() ? settings.cropColour() : 0;
    }
}
