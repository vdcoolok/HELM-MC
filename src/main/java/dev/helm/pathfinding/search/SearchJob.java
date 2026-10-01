package dev.helm.pathfinding.search;

public final class SearchJob {

    private final Search search;
    private final long submittedAt = System.currentTimeMillis();
    private volatile SearchOutcome outcome;
    private volatile boolean done;

    public SearchJob(Search search) {
        this.search = search;
    }

    public void execute() {
        try {
            outcome = search.run(System::currentTimeMillis);
        } finally {
            done = true;
        }
    }

    public Search search() {
        return search;
    }

    public long millis() {
        return System.currentTimeMillis() - submittedAt;
    }

    public boolean done() {
        return done;
    }

    public SearchOutcome outcome() {
        return outcome;
    }
}