package dev.helm.pathfinding.context;

public record Tunables(boolean sprintAllowed,
                       boolean breakAllowed,
                       boolean placeAllowed,
                       boolean parkourAllowed,
                       boolean parkourPlaceAllowed,
                       boolean parkourAscendAllowed,
                       boolean jumpAtBuildLimit,
                       boolean diagonalAscendAllowed,
                       boolean diagonalDescendAllowed,
                       boolean downwardAllowed,
                       boolean magmaWalkAllowed,
                       boolean vinesWalkAllowed,
                       boolean bottomSlabWalkAllowed,
                       boolean assumeStep,
                       boolean assumeWalkOnWater,
                       boolean allowFallIntoLava,
                       int frostWalkerLevel,
                       int minFallHeight,
                       int maxFallHeightNoWater,
                       int maxBuildLevel,
                       double waterWalkSpeed,
                       double placePenalty,
                       double breakAdditionalCost,
                       double jumpPenalty,
                       double waterWalkPenalty) {

    public boolean canSprint() {
        return sprintAllowed;
    }

    public double placementCost() {
        return placeAllowed ? placePenalty : Double.POSITIVE_INFINITY;
    }

}