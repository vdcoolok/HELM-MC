package dev.helm.outline.block;

import java.util.Collection;
import java.util.List;

import net.minecraft.core.BlockPos;

import dev.helm.mine.MineTask;
import dev.helm.setting.OutlineSettings;
import dev.helm.setting.Settings;

public final class BreakingSilhouette implements BlockSilhouetteFeed {

    private static final BreakingSilhouette INSTANCE = new BreakingSilhouette();

    private BreakingSilhouette() {
    }

    public static BreakingSilhouette instance() {
        return INSTANCE;
    }

    @Override
    public Collection<BlockPos> watch() {
        BlockPos breaking = MineTask.instance().breaking();
        if (breaking == null) {
            return List.of();
        }
        return List.of(breaking);
    }

    @Override
    public int colour() {
        OutlineSettings settings = Settings.holder().outline();
        return settings.blocksToBreak() ? settings.breakColour() : 0;
    }
}