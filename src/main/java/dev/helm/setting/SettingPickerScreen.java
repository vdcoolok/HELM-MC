package dev.helm.setting;

import dev.helm.command.CommandResult;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;

public final class SettingPickerScreen {

    public static final String PREFIX = "$set ";

    private SettingPickerScreen() {
    }

    public static CommandResult open() {
        Minecraft.getInstance().setScreenAndShow(new ChatScreen(PREFIX, false));
        return CommandResult.SUCCESS;
    }
}