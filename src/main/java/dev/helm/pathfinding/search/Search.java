package dev.helm.pathfinding.search;

import dev.helm.pathfinding.goal.Goal;
import dev.helm.pathfinding.move.MoveExpander;
import dev.helm.pathfinding.move.MoveKind;
import dev.helm.pathfinding.move.MoveTarget;
import dev.helm.pathfinding.node.Node;
import dev.helm.pathfinding.node.NodeStore;
import dev.helm.pathfinding.path.NodePath;
import dev.helm.pathfinding.world.WorldView;

public final class Search {

    private static final int CLOCK_MASK = (1 << 6) - 1;

    private final int startX;
    private final int startY;
    private final int startZ;
    private final Goal goal;
    private final WorldView world;
    private final MoveExpander[] expanders;
    private final SearchBudget budget;
    private final NodeStore store = new NodeStore();
    private final Frontier frontier = new Frontier();
    private final BestSoFar best = new BestSoFar();
    private final MoveTarget scratch = new MoveTarget();

    private Node startNode;
    private Node furthestConsidered;
    private int visited;
    private SearchOutcome outcome = SearchOutcome.NO_PATH;

    public Search(int startX, int startY, int startZ, Goal goal, WorldView world,
                  MoveExpander[] expanders, SearchBudget budget) {
        this.startX = startX;
        this.startY = startY;
        this.startZ = startZ;
        this.goal = goal;
        this.world = world;
        this.expanders = expanders;
        this.budget = budget;
    }

    public WorldView world() {
        return world;
    }

    public MoveExpander[] expanders() {
        return expanders;
    }

    public int visited() {
        return visited;
    }

    public int stored() {
        return store.size();
    }

    public Node furthestConsidered() {
        return furthestConsidered;
    }

    public SearchOutcome outcome() {
        return outcome;
    }

    public NodePath partial() {
        Node end = best.best(startX, startY, startZ);
        return end == null ? null : NodePath.of(startNode, end);
    }

    public SearchOutcome run(long now, java.util.function.LongSupplier clock) {
        budget.begin(now);
        visited = 0;
        store.clear();
        frontier.clear();
        best.reset();
        startNode = null;
        furthestConsidered = null;

        startNode = store.at(startX, startY, startZ, goal);
        startNode.cost = 0;
        startNode.combined = startNode.estimate;
        frontier.add(startNode);
        best.seed(startNode);

        int lowest = world.lowestLevel();
        int ceiling = lowest + world.levelCount();
        double minImprovement = budget.improvement();

        while (!frontier.isEmpty() && !budget.cancelled()) {
            if ((visited & CLOCK_MASK) == 0 && budget.spent(visited, clock.getAsLong())) {
                break;
            }

            Node current = frontier.takeLowest();
            furthestConsidered = current;
            visited++;

            if (goal.reached(current.x, current.y, current.z)) {
                outcome = SearchOutcome.REACHED_GOAL;
                return outcome;
            }

            for (MoveKind move : MoveKind.order()) {
                if (!viable(current, move, lowest, ceiling)) {
                    continue;
                }
                double stepCost = expand(current, move);
                if (stepCost >= dev.helm.pathfinding.cost.MoveCosts.IMPOSSIBLE) {
                    continue;
                }
                if (stepCost <= 0 || Double.isNaN(stepCost)) {
                    throw new IllegalStateException(move + " produced cost " + stepCost
                            + " from " + current.x + " " + current.y + " " + current.z);
                }
                if (!landedAsDeclared(current, move)) {
                    continue;
                }

                Node neighbour = store.at(scratch.x, scratch.y, scratch.z, goal);
                if (neighbour.cost - (current.cost + stepCost) <= minImprovement) {
                    continue;
                }
                neighbour.adopt(current, stepCost, move);
                if (neighbour.queued()) {
                    frontier.refresh(neighbour);
                } else {
                    frontier.add(neighbour);
                }
                best.offer(neighbour, minImprovement);
                budget.noteProgress(neighbour.distanceFromSq(startX, startY, startZ));
            }
        }

        if (budget.cancelled()) {
            outcome = SearchOutcome.CANCELLED;
        } else {
            outcome = best.best(startX, startY, startZ) == null
                    ? SearchOutcome.NO_PATH
                    : SearchOutcome.PARTIAL_PATH;
        }
        return outcome;
    }

    private boolean viable(Node current, MoveKind move, int lowest, int ceiling) {
        int nextX = current.x + move.offsetX;
        int nextZ = current.z + move.offsetZ;
        if (crossesChunk(current.x, current.z, nextX, nextZ) && !world.loaded(nextX, nextZ)) {
            if (!move.offsetFollowsBlock) {
                budget.countUnloadedCrossing();
            }
            return false;
        }
        int nextY = current.y + move.offsetY;
        if (nextY > ceiling || nextY < lowest) {
            return false;
        }
        return move.offsetFollowsBlock || world.insideBorder(nextX, nextY, nextZ);
    }

    private double expand(Node current, MoveKind move) {
        scratch.clear();
        expanders[move.ordinal()].expand(current.x, current.y, current.z, scratch);
        return scratch.cost;
    }

    private boolean landedAsDeclared(Node current, MoveKind move) {
        if (move.offsetFollowsBlock) {
            return world.insideBorder(scratch.x, scratch.y, scratch.z);
        }
        if (scratch.x != current.x + move.offsetX || scratch.z != current.z + move.offsetZ) {
            throw new IllegalStateException(move + " left the expected column");
        }
        if (!move.levelFollowsBlock && scratch.y != current.y + move.offsetY) {
            throw new IllegalStateException(move + " left the expected level");
        }
        return true;
    }

    private boolean crossesChunk(int fromX, int fromZ, int toX, int toZ) {
        return (toX >> 4) != (fromX >> 4) || (toZ >> 4) != (fromZ >> 4);
    }
}
