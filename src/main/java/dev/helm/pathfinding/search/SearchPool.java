package dev.helm.pathfinding.search;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public final class SearchPool {

    private static final int WORKERS = 4;
    private static final long IDLE_SECONDS = 60L;

    private final ExecutorService workers;

    public SearchPool() {
        this.workers = new ThreadPoolExecutor(WORKERS, Integer.MAX_VALUE, IDLE_SECONDS,
                TimeUnit.SECONDS, new SynchronousQueue<>(), named());
    }

    public void submit(Runnable work) {
        try {
            workers.execute(() -> {
                try {
                    work.run();
                } catch (RuntimeException | Error failure) {
                    dev.helm.diag.Trace.instance().event("search",
                            "a search failed on the worker thread: " + failure);
                }
            });
        } catch (RuntimeException rejected) {
            dev.helm.diag.Trace.instance().event("search",
                    "a search could not be handed to a worker: " + rejected);
        }
    }

    public void shutdown() {
        workers.shutdownNow();
    }

    private static ThreadFactory named() {
        AtomicInteger counter = new AtomicInteger();
        return runnable -> {
            Thread thread = new Thread(runnable, "helm-pathfinder-" + counter.incrementAndGet());
            thread.setDaemon(true);
            thread.setPriority(Thread.NORM_PRIORITY - 1);
            return thread;
        };
    }
}