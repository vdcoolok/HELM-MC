package dev.helm.diag;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public final class TraceClock {

    private static final DateTimeFormatter TIME =
            DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    private static long tick;

    private TraceClock() {
    }

    public static void advance() {
        tick++;
    }

    public static long tick() {
        return tick;
    }

    public static void reset() {
        tick = 0;
    }

    public static String stamp() {
        return LocalTime.now().format(TIME);
    }
}
