package dev.helm.mixin.accessor;

import net.minecraft.client.multiplayer.ClientChunkCache;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientChunkCache.class)
public interface ChunkCacheAccess {

    @Accessor("storage")
    Object helmStorage();
}