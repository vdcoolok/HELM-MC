package dev.helm.aim;

import java.util.random.RandomGenerator;

import dev.helm.setting.LookSettings;

public final class AimProcessor {

    private final RandomGenerator random = RandomGenerator.getDefault();
    private final LookSettings settings;
    private final MouseScale scale;

    private double yawOffset;
    private double pitchOffset;

    public AimProcessor(LookSettings settings, MouseScale scale) {
        this.settings = settings;
        this.scale = scale;
    }

    public void tick() {
        yawOffset = (random.nextDouble() - 0.5D) * settings.randomLooking();
        pitchOffset = (random.nextDouble() - 0.5D) * settings.randomLooking();
        double wobble = random.nextDouble() - 0.5D;
        if (Math.abs(wobble) < 0.1D) {
            wobble *= 4;
        }
        yawOffset += wobble * settings.randomLookingWobble();
    }

    public Aim from(Aim current, Aim desired) {
        double wantedYaw = desired.yaw();
        double wantedPitch = desired.pitch();
        if (wantedPitch == current.pitch()) {
            wantedPitch = levelOff(wantedPitch);
        }
        wantedYaw += yawOffset;
        wantedPitch += pitchOffset;
        return new Aim(
                scale.apply((float) current.yaw(), (float) wantedYaw),
                scale.apply((float) current.pitch(), (float) wantedPitch));
    }

    public Aim next(Aim current, Aim desired) {
        Aim result = from(current, desired);
        tick();
        return result;
    }

    private static double levelOff(double pitch) {
        if (pitch < -20) {
            return pitch + 1;
        }
        if (pitch > 10) {
            return pitch - 1;
        }
        return pitch;
    }
}
