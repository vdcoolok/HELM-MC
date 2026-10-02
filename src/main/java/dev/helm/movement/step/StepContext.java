package dev.helm.movement.step;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

import dev.helm.pathfinding.world.BlockView;
import dev.helm.pathfinding.world.block.StandHeight;
import dev.helm.pathfinding.world.block.WalkRules;
import dev.helm.setting.LookSettings;
import dev.helm.setting.MovementSettings;

public final class StepContext {

    private final BlockView world;
    private final WalkRules walk;
    private final LookSettings look;
    private final MovementSettings movement;

    public StepContext(BlockView world, WalkRules walk, LookSettings look, MovementSettings movement) {
        this.world = world;
        this.walk = walk;
        this.look = look;
        this.movement = movement;
    }

    public LocalPlayer player() {
        return Minecraft.getInstance().player;
    }

    public int[] feetOf(LocalPlayer who) {
        return StandHeight.of(world, who.getX(), who.getY(), who.getZ());
    }

    public BlockView world() {
        return world;
    }

    public WalkRules walk() {
        return walk;
    }

    public LookSettings look() {
        return look;
    }

    public MovementSettings movement() {
        return movement;
    }

    public int[] feet() {
        LocalPlayer player = player();
        return feetOf(player);
    }
}
