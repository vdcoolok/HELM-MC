package dev.helm.command.popup;

import net.minecraft.client.input.KeyEvent;
import org.lwjgl.glfw.GLFW;

public final class PopupKeys {

    private PopupKeys() {
    }

    public static boolean isUp(KeyEvent event) {
        return matches(event, GLFW.GLFW_KEY_UP);
    }

    public static boolean isDown(KeyEvent event) {
        return matches(event, GLFW.GLFW_KEY_DOWN);
    }

    public static boolean isLeft(KeyEvent event) {
        return matches(event, GLFW.GLFW_KEY_LEFT);
    }

    public static boolean isRight(KeyEvent event) {
        return matches(event, GLFW.GLFW_KEY_RIGHT);
    }

    public static boolean isTab(KeyEvent event) {
        return matches(event, GLFW.GLFW_KEY_TAB);
    }

    public static boolean isSpace(KeyEvent event) {
        return matches(event, GLFW.GLFW_KEY_SPACE);
    }

    private static boolean matches(KeyEvent event, int... keys) {
        if (event == null) {
            return false;
        }
        int key = event.key();
        for (int candidate : keys) {
            if (key == candidate) {
                return true;
            }
        }
        return false;
    }
}