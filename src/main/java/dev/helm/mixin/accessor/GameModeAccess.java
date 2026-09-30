package dev.helm.mixin.accessor;

import dev.helm.access.GameModeControl;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MultiPlayerGameMode.class)
public abstract class GameModeAccess implements GameModeControl {

    @Accessor("isDestroying")
    @Override
    public abstract boolean hitting();

    @Accessor("isDestroying")
    @Override
    public abstract void setHitting(boolean value);

    @Accessor("destroyBlockPos")
    @Override
    public abstract BlockPos currentTarget();

    @Accessor("destroyDelay")
    @Override
    public abstract void setDestroyDelay(int value);

    @Invoker("ensureHasSentCarriedItem")
    @Override
    public abstract void syncCarriedItem();
}
