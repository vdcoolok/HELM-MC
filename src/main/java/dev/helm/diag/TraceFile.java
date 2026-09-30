package dev.helm.diag;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public final class TraceFile {

    private static final long SOFT_CAP_BYTES = 512L * 1024L;
    private static final int TRIM_TO_LINES = 4000;

    private final Path file;
    private long written;

    TraceFile(Path file) {
        this.file = file;
    }

    public void begin() {
        try {
            Path parent = file.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.deleteIfExists(file);
            written = 0;
        } catch (IOException ignored) {
            written = Long.MAX_VALUE;
        }
    }

    public void append(String line) {
        if (written == Long.MAX_VALUE) {
            return;
        }
        try {
            Files.writeString(file, line + System.lineSeparator(), StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            written += line.length() + 1L;
        } catch (IOException ignored) {
            written = Long.MAX_VALUE;
        }
    }

    public void trim() {
        if (written < SOFT_CAP_BYTES || written == Long.MAX_VALUE) {
            return;
        }
        try {
            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            if (lines.size() <= TRIM_TO_LINES) {
                return;
            }
            List<String> kept = new ArrayList<>(lines.subList(lines.size() - TRIM_TO_LINES,
                    lines.size()));
            StringBuilder rebuilt = new StringBuilder();
            for (String line : kept) {
                rebuilt.append(line).append(System.lineSeparator());
            }
            Files.writeString(file, rebuilt.toString(), StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            written = rebuilt.length();
        } catch (IOException ignored) {
            written = Long.MAX_VALUE;
        }
    }

    public Path path() {
        return file;
    }

    public long size() {
        return written;
    }
}
