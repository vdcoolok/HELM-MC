package dev.helm.pathfinding.context;

import dev.helm.setting.MovementSettings;
import dev.helm.setting.Settings;
import dev.helm.pathfinding.cost.MoveCosts;

public final class TunablesFactory {

    private TunablesFactory() {
    }

    public static Tunables fromSettings() {
        return from(Settings.holder().movement());
    }

    public static Tunables fromSettings(java.util.function.UnaryOperator<Tunables> adjust) {
        return adjust.apply(fromSettings());
    }

    public static Tunables from(MovementSettings settings) {
        return new Tunables(settings.sprintAllowed(),
                settings.allowBreak(),
                settings.allowPlace(),
                settings.parkourAllowed(),
                settings.parkourPlaceAllowed(),
                settings.parkourAscendAllowed(),
                settings.jumpAtBuildLimit(),
                settings.diagonalAscendAllowed(),
                settings.diagonalDescendAllowed(),
                settings.downwardAllowed(),
                settings.magmaWalkAllowed(),
                settings.vinesWalkAllowed(),
                settings.bottomSlabWalkAllowed(),
                settings.assumeStep(),
                settings.assumeWalkOnWater(),
                false,
                0,
                3,
                settings.maxFallHeightNoWater(),
                settings.maxFallHeightBucket(),
                waterStepCost(),
                settings.placementPenalty(),
                settings.breakAdditionalCost(),
                settings.jumpPenalty(),
                settings.waterWalkPenalty());
    }

    private static double waterStepCost() {
        return MoveCosts.WALK_ONE;
    }
}
