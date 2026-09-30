package dev.helm.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import dev.helm.macro.MacroStoreException;
import net.minecraft.client.Minecraft;

public final class HelmStorage {

    private static final String ROOT = "HELM";
    private static final String MACROS = "macros";

    private static boolean alreadyReported;

    private HelmStorage() {
    }

    public static Path root() {
        return gameDirectory().resolve(ROOT);
    }

    public static Path macroDirectory() {
        return root().resolve(MACROS);
    }

    public static Path ensureMacroDirectory() {
        return create(macroDirectory());
    }

    public static Optional<String> prepare() {
        try {
            Files.createDirectories(macroDirectory());
            alreadyReported = false;
            return Optional.empty();
        } catch (IOException unwritable) {
            if (alreadyReported) {
                return Optional.empty();
            }
            alreadyReported = true;
            return Optional.of("Could not create its data folder at " + macroDirectory()
                    + ". Macro features are unavailable until this is fixed.");
        }
    }

    private static Path create(Path directory) {
        try {
            Files.createDirectories(directory);
        } catch (IOException unwritable) {
            throw new MacroStoreException("Could not create " + directory, unwritable);
        }
        return directory;
    }

    private static Path gameDirectory() {
        return Minecraft.getInstance().gameDirectory.toPath();
    }
}
