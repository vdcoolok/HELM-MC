package dev.helm.control;

import net.minecraft.client.player.ClientInput;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.Vec2;

public final class SteeringInput extends ClientInput {

    private static final double SNEAK_DAMPING = 0.3D;

    private final ControlState controls;

    public SteeringInput(ControlState controls) {
        this.controls = controls;
    }

    @Override
    public void tick() {
        float strafe = 0.0F;
        float forward = 0.0F;
        boolean jumping = controls.isDown(Control.JUMP);
        boolean ahead = controls.isDown(Control.MOVE_FORWARD);
        if (ahead) {
            forward++;
        }
        boolean back = controls.isDown(Control.MOVE_BACK);
        if (back) {
            forward--;
        }
        boolean left = controls.isDown(Control.MOVE_LEFT);
        if (left) {
            strafe++;
        }
        boolean right = controls.isDown(Control.MOVE_RIGHT);
        if (right) {
            strafe--;
        }
        boolean sneaking = controls.isDown(Control.SNEAK);
        if (sneaking) {
            strafe *= SNEAK_DAMPING;
            forward *= SNEAK_DAMPING;
        }
        this.moveVector = new Vec2(strafe, forward);
        this.keyPresses = new Input(ahead, back, left, right, jumping, sneaking,
                controls.isDown(Control.SPRINT));
    }

    public static float yawOf(float playerYaw) {
        return Mth.wrapDegrees(playerYaw);
    }
}
