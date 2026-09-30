package dev.helm.tools;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import dev.helm.setting.MiningSettings;
import dev.helm.setting.MovementSettings;
import dev.helm.setting.Settings;
import dev.helm.world.PlayerInventory;

public final class ToolChooser {

    private ToolChooser() {
    }

    public static void forBlock(BlockPos pos) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) {
            return;
        }
        MiningSettings mining = Settings.holder().mining();
        MovementSettings movement = Settings.holder().movement();
        if (!mining.autoTool() || movement.assumeExternalAutoTool()) {
            return;
        }
        BlockState state = client.level.getBlockState(pos);
        PlayerInventory inventory = new PlayerInventory(client.player);
        int slot = BestSlot.forBlock(state.getBlock(), mining.preferSilkTouch(), false,
                inventory, mining);
        inventory.selectSlot(slot);
    }
}
