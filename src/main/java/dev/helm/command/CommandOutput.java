package dev.helm.command;

import net.minecraft.network.chat.Component;

public interface CommandOutput {

    void feedback(Component message);

    void error(Component message);
}
