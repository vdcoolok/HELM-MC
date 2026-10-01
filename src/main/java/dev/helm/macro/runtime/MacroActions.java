package dev.helm.macro.runtime;

import dev.helm.aim.Aim;
import dev.helm.aim.LookController;
import dev.helm.input.InputBinding;
import dev.helm.input.InputKind;
import dev.helm.macro.MacroStatement;
import dev.helm.navigate.Destination;
import dev.helm.navigate.Journey;
import dev.helm.navigate.NavigatorAgent;
import dev.helm.pathfinding.goal.BlockGoal;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import com.mojang.blaze3d.platform.InputConstants;

final class MacroActions {

    private MacroActions() {
    }

    static MacroRunner.Ongoing walk(MacroStatement.Move move) {
        var client = Minecraft.getInstance();
        if (client.player == null) {
            throw MacroFailure.noWorld();
        }
        int x = (int) Math.floor(move.x());
        int y = (int) Math.floor(move.y());
        int z = (int) Math.floor(move.z());
        NavigatorAgent.instance().pilot().forgetDestination();
        var feet = client.player.blockPosition();
        if (feet.getX() == x && feet.getY() == y && feet.getZ() == z) {
            return null;
        }
        return MacroJourney.towards(NavigatorAgent.instance(), move);
    }

    static MacroRunner.Ongoing anchor(MacroStatement.Anchor anchor) {
        var client = Minecraft.getInstance();
        if (client.player == null) {
            throw MacroFailure.noWorld();
        }
        int x = (int) Math.floor(anchor.x());
        int y = (int) Math.floor(anchor.y());
        int z = (int) Math.floor(anchor.z());
        var agent = NavigatorAgent.instance();
        agent.pilot().anchorAt(new Destination(x, y, z));
        var feet = client.player.blockPosition();
        if (feet.getX() == x && feet.getY() == y && feet.getZ() == z) {
            return null;
        }
        return MacroJourney.holding(agent, new Destination(x, y, z));
    }

    private static final class MacroWalker implements MacroRunner.Ongoing {

        private final NavigatorAgent agent;
        private final dev.helm.pathfinding.search.SearchJob job;
        private final int x;
        private final int y;
        private final int z;
        private boolean started;

        MacroWalker(NavigatorAgent agent, dev.helm.pathfinding.search.SearchJob job,
                    int x, int y, int z) {
            this.agent = agent;
            this.job = job;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public boolean done() {
            if (started) {
                return !agent.pilot().isWalking();
            }
            if (!job.done()) {
                return false;
            }
            started = true;
            Journey.Result result = Journey.collect(job.search(), agent.navigator().blocks(),
                    agent.navigator().walk());
            if (result.arrived()) {
                return true;
            }
            if (!result.usable()) {
                throw MacroFailure.unreachable(x, y, z);
            }
            agent.pilot().travel(result.route());
            return !agent.pilot().isWalking();
        }
    }

    static void look(MacroStatement.Look look) {
        var client = Minecraft.getInstance();
        if (client.player == null) {
            throw MacroFailure.noWorld();
        }
        client.player.setYRot((float) look.rotation().yaw());
        client.player.setXRot((float) look.rotation().pitch());
    }

    static void gaze(MacroStatement.Gaze gaze) {
        LookController.instance().hold(new Aim(gaze.rotation().yaw(), gaze.rotation().pitch()));
    }

    static void press(InputBinding input, boolean down) {
        key(input).setDown(down);
    }

    static MacroRunner.Ongoing tap(InputBinding input) {
        KeyMapping key = key(input);
        key.setDown(true);
        return () -> {
            key.setDown(false);
            return true;
        };
    }

    private static KeyMapping key(InputBinding input) {
        Options options = Minecraft.getInstance().options;
        if (options == null) {
            throw MacroFailure.noWorld();
        }
        if (input.kind() == InputKind.MOUSE) {
            return switch (input.label()) {
                case "M1" -> options.keyAttack;
                case "M2" -> options.keyUse;
                default -> options.keyPickItem;
            };
        }
        if (input.kind() == InputKind.SCROLL) {
            throw MacroFailure.unusable(input);
        }
        var wanted = InputConstants.Type.KEYSYM.getOrCreate(input.code());
        for (KeyMapping candidate : keys(options)) {
            if (candidate.matches(wanted)) {
                return candidate;
            }
        }
        throw MacroFailure.unusable(input);
    }

    private static KeyMapping[] keys(Options options) {
        return new KeyMapping[] {
            options.keyUp, options.keyDown, options.keyLeft, options.keyRight,
            options.keyJump, options.keyShift, options.keySprint,
            options.keyAttack, options.keyUse, options.keyPickItem,
            options.keyInventory, options.keySwapOffhand, options.keyDrop,
            options.keyChat, options.keyCommand, options.keyPlayerList,
            options.keyScreenshot, options.keyTogglePerspective, options.keySmoothCamera,
            options.keyFullscreen, options.keyAdvancements,
            options.keySaveHotbarActivator, options.keyLoadHotbarActivator,
            options.keyQuickActions, options.keyToggleGui, options.keySpectatorOutlines
        };
    }
}