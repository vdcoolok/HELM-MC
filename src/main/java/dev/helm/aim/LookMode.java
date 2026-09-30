package dev.helm.aim;

public enum LookMode {

    CLIENT,
    SERVER,
    NONE;

    public static LookMode resolve(boolean gliding, boolean wantsBlockInteract,
                                   dev.helm.setting.LookSettings settings) {
        if (gliding) {
            return settings.elytraFreeLook() ? SERVER : CLIENT;
        }
        if (settings.freeLook()) {
            if (wantsBlockInteract) {
                return settings.blockFreeLook() ? SERVER : CLIENT;
            }
            return settings.antiCheatCompatibility() ? SERVER : NONE;
        }
        return CLIENT;
    }
}
