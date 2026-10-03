package dev.helm.drops;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;


public final class DropCache {

    private static final long SITE_TICKS = 6L;
    private static final long KEEP_TICKS = 6000L;

    private static final DropCache INSTANCE = new DropCache();

    private final BreakLedger ledger = new BreakLedger();
    private final DropInterest interest = new DropInterest();
    private long tick;

    private DropCache() {
    }

    public static DropCache instance() {
        return INSTANCE;
    }

    public void remember(BlockPos pos) {
        ledger.remember(pos, tick);
    }

    public void onTick() {
        tick++;
        ledger.expire(tick, SITE_TICKS);
        interest.expire(tick, KEEP_TICKS);
        if (ledger.empty()) {
            return;
        }
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        DropScan.learn(level, ledger, interest, tick, KEEP_TICKS);
    }

    public boolean wanted(ItemStack stack) {
        return !stack.isEmpty() && wanted(stack.getItem());
    }

    public boolean wanted(Item item) {
        return interest.wanted(item, tick, KEEP_TICKS);
    }

    public void clear() {
        tick = 0;
        ledger.clear();
        interest.clear();
    }
}
