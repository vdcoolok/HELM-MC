package dev.helm.outline.block;

import java.util.Collection;
import java.util.List;

import net.minecraft.core.BlockPos;

import dev.helm.mine.MineTask;
import dev.helm.setting.OutlineSettings;
import dev.helm.setting.Settings;

public final class MineTargetSilhouette implements BlockSilhouetteFeed {

    private static final MineTargetSilhouette INSTANCE = new MineTargetSilhouette();

    private MineTargetSilhouette() {
    }

    public static MineTargetSilhouette instance() {
        return INSTANCE;
    }

    @Override
    public Collection<BlockPos> watch() {
        if (!Settings.holder().mining().renderTargets()) {
            return List.of();
        }
        return MineTask.instance().targets();
    }

    @Override
    public int colour() {
        OutlineSettings settings = Settings.holder().outline();
        return Settings.holder().mining().renderTargets() ? settings.targetColour() : 0;
    }
}