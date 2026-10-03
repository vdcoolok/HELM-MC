package dev.helm.world.read.section;

import java.lang.reflect.Field;

import net.minecraft.util.BitStorage;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.Palette;

import dev.helm.diag.Trace;

public final class ContainerReading {

    private static final String CONTAINER_DATA = "data";
    private static final String DATA_PALETTE = "palette";
    private static final String DATA_STORAGE = "storage";

    private static boolean resolved;
    private static Field containerData;
    private static Field dataPalette;
    private static Field dataStorage;

    private ContainerReading() {
    }

    @SuppressWarnings("unchecked")
    public static SectionPacking of(PalettedContainer<BlockState> container) {
        if (!resolve()) {
            return null;
        }
        try {
            Object data = containerData.get(container);
            return new SectionPacking((Palette<BlockState>) dataPalette.get(data),
                    (BitStorage) dataStorage.get(data));
        } catch (ReflectiveOperationException | RuntimeException unreadable) {
            giveUp("a chunk section could not be unpacked: " + unreadable);
            return null;
        }
    }

    private static synchronized boolean resolve() {
        if (resolved) {
            return containerData != null;
        }
        resolved = true;
        try {
            containerData = field(PalettedContainer.class, CONTAINER_DATA);
            if (containerData == null) {
                return giveUp("chunk sections keep their packed data in a field this build "
                        + "does not have, so sections are read one block at a time");
            }
            Class<?> data = containerData.getType();
            dataPalette = field(data, DATA_PALETTE);
            dataStorage = field(data, DATA_STORAGE);
            if (dataPalette == null || dataStorage == null) {
                containerData = null;
                return giveUp("chunk section data has no palette and storage this build can "
                        + "read, so sections are read one block at a time");
            }
            return true;
        } catch (RuntimeException | LinkageError broken) {
            containerData = null;
            return giveUp("chunk sections could not be inspected: " + broken);
        }
    }

    private static Field field(Class<?> owner, String name) {
        for (Field candidate : owner.getDeclaredFields()) {
            if (candidate.getName().equals(name)) {
                candidate.setAccessible(true);
                return candidate;
            }
        }
        return null;
    }

    private static boolean giveUp(String why) {
        containerData = null;
        dataPalette = null;
        dataStorage = null;
        Trace.instance().event("search", why);
        return false;
    }
}
