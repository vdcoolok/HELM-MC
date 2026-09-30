package dev.helm.mixin.accessor;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MultiPlayerGameMode.class)
public abstract class GameModeAccessMixin implements GameModeAccess {

    @Accessor("isDestroying")
    @Override
    public abstract boolean helmIsHitting();

    @Accessor("isDestroying")
    @Override
    public abstract void helmSetHitting(boolean value);

    @Accessor("destroyBlockPos")
    @Override
    public abstract BlockPos helmCurrentTarget();

    @Accessor("destroyDelay")
    @Override
    public abstract void helmSetDestroyDelay(int value);

    @Invoker("ensureHasSentCarriedItem")
    @Override
    public abstract void helmSyncCarriedItem();
}
