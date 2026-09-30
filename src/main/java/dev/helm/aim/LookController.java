package dev.helm.aim;

import java.util.ArrayDeque;
import java.util.Deque;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

import dev.helm.setting.LookSettings;

public final class LookController {

    private static final LookController INSTANCE = new LookController();

    private Aim target;
    private LookMode mode = LookMode.NONE;
    private Aim sentToServer;
    private Aim previous;
    private Aim jumping;
    private AimProcessor processor;
    private final Deque<Double> yawTrail = new ArrayDeque<>();
    private final Deque<Double> pitchTrail = new ArrayDeque<>();

    private LookController() {
    }

    public static LookController instance() {
        return INSTANCE;
    }

    public void updateSettings(MouseScale scale, LookSettings settings) {
        this.processor = new AimProcessor(settings, scale);
    }

    public void aimAt(Aim wanted, boolean blockInteract) {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        if (player == null) {
            return;
        }
        if (processor == null) {
            updateSettings(defaultScale(), dev.helm.setting.Settings.holder().look());
        }
        this.target = wanted;
        this.mode = LookMode.resolve(player.isFallFlying(), blockInteract,
                dev.helm.setting.Settings.holder().look());
    }

    public void clear() {
        target = null;
        jumping = null;
        mode = LookMode.NONE;
    }

    public void onServerRotation(float yaw, float pitch) {
        sentToServer = new Aim(yaw, pitch);
    }

    public void onWorldChange() {
        sentToServer = null;
        target = null;
    }

    public Aim serverRotation() {
        return sentToServer;
    }

    public void tick() {
        if (processor != null) {
            processor.tick();
        }
    }

    public Aim beforePlayerUpdate() {
        if (target == null || mode == LookMode.NONE || processor == null) {
            return null;
        }
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return null;
        }
        previous = new Aim(player.getYRot(), player.getXRot());
        return processor.from(previous, target);
    }

    public void afterPlayerUpdate() {
        if (previous == null) {
            target = null;
            return;
        }
        LookSettings settings = dev.helm.setting.Settings.holder().look();
        remember(target.yaw(), settings.smoothLookTicks());
        rememberPitch(target.pitch(), settings.smoothLookTicks());
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            previous = null;
            target = null;
            return;
        }
        if (mode == LookMode.SERVER) {
            player.setYRot((float) previous.yaw());
            player.setXRot((float) previous.pitch());
        } else if (player.isFallFlying() ? settings.elytraSmoothLook() : settings.smoothLook()) {
            player.setYRot((float) average(yawTrail, previous.yaw()));
            if (player.isFallFlying()) {
                player.setXRot((float) average(pitchTrail, previous.pitch()));
            }
        }
        previous = null;
        target = null;
    }

    public Aim quantise(net.minecraft.world.entity.Entity viewer, Aim desired) {
        if (processor == null) {
            updateSettings(defaultScale(), dev.helm.setting.Settings.holder().look());
        }
        return processor.from(BlockReach.current(viewer), desired);
    }

    public Aim forMovementPacket() {
        if (target == null || processor == null) {
            return null;
        }
        LocalPlayer player = Minecraft.getInstance().player;
        return player == null ? null : processor.from(BlockReach.current(player), target);
    }

    public boolean hasMovementAim() {
        return target != null && processor != null;
    }

    public Aim movementAim() {
        if (target == null || processor == null) {
            return null;
        }
        LocalPlayer player = Minecraft.getInstance().player;
        return player == null ? null : processor.from(BlockReach.current(player), target);
    }

    public void beginJump() {
        if (target == null || processor == null) {
            jumping = null;
            return;
        }
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            jumping = null;
            return;
        }
        jumping = processor.from(BlockReach.current(player), target);
    }

    public Float jumpYaw() {
        return jumping == null ? null : (float) jumping.yaw();
    }

    public void endJump() {
        jumping = null;
    }

    public void keepCameraYaw() {
        if (target == null || processor == null) {
            return;
        }
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        player.setYRot((float) processor.from(BlockReach.current(player), target).yaw());
    }

    private void remember(double value, int window) {
        yawTrail.addLast(value);
        while (yawTrail.size() > window) {
            yawTrail.removeFirst();
        }
    }

    private void rememberPitch(double value, int window) {
        pitchTrail.addLast(value);
        while (pitchTrail.size() > window) {
            pitchTrail.removeFirst();
        }
    }

    private static double average(Deque<Double> trail, double fallback) {
        return trail.stream().mapToDouble(Double::doubleValue).average().orElse(fallback);
    }

    private static MouseScale defaultScale() {
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.options == null) {
            return new MouseScale(0.5D);
        }
        return MouseScale.fromOptions(client.options.sensitivity().get());
    }

    public void report(String message) {
        Minecraft client = Minecraft.getInstance();
        if (client != null && client.gui != null && client.gui.hud != null) {
            client.gui.hud.getChat().addClientSystemMessage(Component.literal(message));
        }
    }
}
