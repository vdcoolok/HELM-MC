package dev.helm.setting;

import java.io.IOException;
import java.nio.file.Path;

public final class SettingsSaveFailure {

    private static boolean reported;

    private SettingsSaveFailure() {
    }

    public static void report(Path file, IOException cause) {
        if (reported) {
            return;
        }
        reported = true;
        ClientNotice.warn("Could not write settings to " + file
                + ". Your changes apply to this session only.");
    }
}
