package dev.helm.mixin.accessor;

import java.util.concurrent.atomic.AtomicReferenceArray;

import net.minecraft.world.level.chunk.LevelChunk;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "net.minecraft.client.multiplayer.ClientChunkCache$Storage")
public interface ChunkStorageAccess {

    @Accessor("chunks")
    AtomicReferenceArray<LevelChunk> helmChunks();
}