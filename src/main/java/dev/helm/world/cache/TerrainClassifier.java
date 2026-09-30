package dev.helm.world.cache;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.TallGrassBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.Vec3;
import dev.helm.pathfinding.world.block.Hazards;
import dev.helm.pathfinding.world.block.LiquidRules;
import dev.helm.world.read.BlockReader;

public final class TerrainClassifier {

    private final LevelChunk chunk;
    private final Level level;
    private final int floor;
    private final int originX;
    private final int originZ;
    private final BlockPos.MutableBlockPos scratch = new BlockPos.MutableBlockPos();

    public TerrainClassifier(LevelChunk chunk) {
        this.chunk = chunk;
        this.level = chunk.getLevel();
        DimensionType dimension = level.dimensionType();
        this.floor = dimension.minY();
        this.originX = chunk.getPos().x() << 4;
        this.originZ = chunk.getPos().z() << 4;
    }

    public TerrainKind at(int x, int level0, int z) {
        return classify(read(x, level0, z), x, level0, z);
    }

    public BlockState read(int x, int y, int z) {
        int relative = y - floor;
        LevelChunkSection[] sections = chunk.getSections();
        int section = relative >> 4;
        if (section < 0 || section >= sections.length) {
            return BlockReader.AIR;
        }
        LevelChunkSection at = sections[section];
        if (at == null || at.hasOnlyAir()) {
            return BlockReader.AIR;
        }
        return at.getBlockState(x & 15, relative & 15, z & 15);
    }

    private TerrainKind classify(BlockState state, int x, int y, int z) {
        if (LiquidRules.water(state)) {
            return waterKind(state, x, y, z);
        }
        if (Hazards.avoidWalkingInto(state) || bottomSlab(state)) {
            return TerrainKind.AVOID;
        }
        if (nonBlocking(state)) {
            return TerrainKind.AIR;
        }
        return TerrainKind.SOLID;
    }

    private TerrainKind waterKind(BlockState state, int x, int y, int z) {
        if (spreading(read(x, y, z))) {
            return TerrainKind.AVOID;
        }
        if (nextToSpreadingWater(x, y, z)) {
            return TerrainKind.AVOID;
        }
        if (!onChunkEdge(x, z)) {
            return TerrainKind.WATER;
        }
        Vec3 flow = state.getFluidState().getFlow(level,
                scratch.set(originX + x, y, originZ + z));
        if (flow.x != 0.0D || flow.z != 0.0D) {
            return TerrainKind.WATER;
        }
        return TerrainKind.AVOID;
    }

    private boolean nextToSpreadingWater(int x, int y, int z) {
        return (x != 15 && spreading(read(x + 1, y, z)))
                || (x != 0 && spreading(read(x - 1, y, z)))
                || (z != 15 && spreading(read(x, y, z + 1)))
                || (z != 0 && spreading(read(x, y, z - 1)));
    }

    private boolean spreading(BlockState state) {
        return LiquidRules.flowing(state);
    }


    private boolean onChunkEdge(int x, int z) {
        return x == 0 || x == 15 || z == 0 || z == 15;
    }

    private static boolean bottomSlab(BlockState state) {
        return state.getBlock() instanceof SlabBlock
                && state.getValue(SlabBlock.TYPE) == SlabType.BOTTOM;
    }

    private static boolean nonBlocking(BlockState state) {
        return state.getBlock() instanceof AirBlock
                || state.getBlock() instanceof TallGrassBlock
                || state.getBlock() instanceof DoublePlantBlock
                || state.getBlock() instanceof FlowerBlock;
    }
}
