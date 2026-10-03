package dev.helm.outline.block;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import net.minecraft.core.BlockPos;

import dev.helm.movement.Route;
import dev.helm.navigate.NavigatorAgent;
import dev.helm.setting.OutlineSettings;
import dev.helm.setting.Settings;

public final class RouteBreakSilhouette implements BlockSilhouetteFeed {

    private static final RouteBreakSilhouette INSTANCE = new RouteBreakSilhouette();

    private RouteBreakSilhouette() {
    }

    public static RouteBreakSilhouette instance() {
        return INSTANCE;
    }

    @Override
    public Collection<BlockPos> watch() {
        OutlineSettings settings = Settings.holder().outline();
        if (!settings.blocksToBreak()) {
            return List.of();
        }
        var pilot = NavigatorAgent.instance().pilot();
        if (!pilot.isWalking()) {
            return List.of();
        }
        Route route = pilot.route();
        Set<int[]> blocks = route.blocksToBreak();
        if (blocks.isEmpty()) {
            return List.of();
        }
        BlockPos[] found = new BlockPos[blocks.size()];
        int index = 0;
        for (int[] block : blocks) {
            found[index++] = new BlockPos(block[0], block[1], block[2]);
        }
        return List.of(found);
    }

    @Override
    public int colour() {
        OutlineSettings settings = Settings.holder().outline();
        return settings.blocksToBreak() ? settings.breakColour() : 0;
    }
}
