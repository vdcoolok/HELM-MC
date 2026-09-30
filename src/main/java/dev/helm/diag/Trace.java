package dev.helm.diag;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import dev.helm.storage.HelmStorage;

public final class Trace {

    private static final String FILE_NAME = "debuglogs.log";
    private static final long PULSE_MILLIS = 2000L;

    private static final Trace INSTANCE = new Trace();

    private final Map<String, String> lastSeen = new HashMap<>();
    private final Map<String, Long> lastPulse = new HashMap<>();
    private final Map<String, Integer> suppressed = new HashMap<>();

    private TraceFile file;
    private long lines;
    private boolean dead;

    private Trace() {
    }

    public static Trace instance() {
        return INSTANCE;
    }

    public void start() {
        if (file != null || dead) {
            return;
        }
        try {
            Path path = HelmStorage.root().resolve(FILE_NAME);
            file = new TraceFile(path);
            file.begin();
            lastSeen.clear();
            lastPulse.clear();
            suppressed.clear();
            lines = 0;
            TraceClock.reset();
        } catch (RuntimeException ignored) {
            dead = true;
            file = null;
        }
    }

    public Path path() {
        return file == null ? null : file.path();
    }

    public long lineCount() {
        return lines;
    }

    public void event(String area, String message) {
        write(area, message);
    }

    public void repeat(String key, String area, String message) {
        if (file == null || dead) {
            return;
        }
        String previous = lastSeen.put(key, message);
        if (message.equals(previous)) {
            count(key, area, message);
            return;
        }
        if (previous != null) {
            write(area, message + "   (was: " + previous + ")");
        } else {
            write(area, message);
        }
    }

    public void pulse(String key, String area, String message) {
        if (file == null || dead) {
            return;
        }
        long now = System.nanoTime();
        Long previous = lastPulse.get(key);
        if (previous != null && (now - previous) / 1_000_000L < PULSE_MILLIS) {
            count(key, area, message);
            return;
        }
        lastPulse.put(key, now);
        write(area, message);
    }

    public void barrier(String area) {
        if (file == null || dead) {
            return;
        }
        write(area, "----");
    }

    private void count(String key, String area, String message) {
        int count = suppressed.merge(key, 1, Integer::sum);
        if (count == 25 || count == 100 || count == 1000) {
            write(area, message + "   (unchanged, now " + count + " ticks)");
        }
    }

    private void write(String area, String message) {
        if (file == null || dead) {
            return;
        }
        try {
            file.append(String.format("[%s t=%-7d %-9s] %s", TraceClock.stamp(),
                    TraceClock.tick(), area, message));
            lines++;
            if ((lines & 0x3F) == 0) {
                file.trim();
            }
        } catch (RuntimeException ignored) {
            dead = true;
        }
    }
}
