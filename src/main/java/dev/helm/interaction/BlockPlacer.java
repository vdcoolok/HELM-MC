package dev.helm.interaction;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;

import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import dev.helm.setting.Settings;

public final class BlockPlacer {

    private static final int BASE_DELAY = 1;

    private int delay;

    public void tick(boolean wantsPlace) {
        if (delay > 0) {
            delay--;
            return;
        }
        Minecraft client = Minecraft.getInstance();
        MultiPlayerGameMode mode = client.gameMode;
        net.minecraft.client.player.LocalPlayer player = client.player;
        HitResult trace = client.hitResult;
        if (!wantsPlace || mode == null || player == null || client.level == null
                || player.isUsingItem() || trace == null || trace.getType() != HitResult.Type.BLOCK) {
            return;
        }
        delay = Settings.holder().movement().rightClickSpeed() - BASE_DELAY;
        BlockHitResult block = (BlockHitResult) trace;
        for (InteractionHand hand : InteractionHand.values()) {
            if (mode.useItemOn(player, hand, block) == InteractionResult.SUCCESS) {
                player.swing(hand);
                return;
            }
            if (!player.getItemInHand(hand).isEmpty()
                    && mode.useItem(player, hand) == InteractionResult.SUCCESS) {
                return;
            }
        }
    }
}
