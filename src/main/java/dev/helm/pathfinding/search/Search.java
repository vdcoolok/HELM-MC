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

    public int visited() {
        return visited;
    }

    public int stored() {
        return store.size();
    }

    public Node furthestConsidered() {
        return furthestConsidered;
    }

    public NodePath partial() {
        Node end = best.best(startX, startY, startZ);
        return end == null ? null : NodePath.of(startNode, end);
    }

    public SearchOutcome run(long now, java.util.function.LongSupplier clock) {
        budget.begin(now);
        visited = 0;

        startNode = store.at(startX, startY, startZ, goal);
        startNode.cost = 0;
        startNode.combined = startNode.estimate;
        frontier.add(startNode);
        best.seed(startNode);

        int lowest = world.lowestLevel();
        int ceiling = lowest + world.levelCount();
        double minImprovement = budget.improvement();

        while (!frontier.isEmpty() && !budget.cancelled()) {
            if (budget.spent(visited, clock.getAsLong())) {
                break;
            }

            Node current = frontier.takeLowest();
            furthestConsidered = current;
            visited++;

            if (goal.reached(current.x, current.y, current.z)) {
                return SearchOutcome.REACHED_GOAL;
            }

            for (MoveKind move : MoveKind.order()) {
                int nextX = current.x + move.offsetX;
                int nextZ = current.z + move.offsetZ;

                if (crossesChunk(current.x, current.z, nextX, nextZ) && !world.loaded(nextX, nextZ)) {
                    if (!move.offsetFollowsBlock) {
                        budget.countUnloadedCrossing();
                    }
                    continue;
                }
                if (!move.offsetFollowsBlock && !world.insideBorder(nextX, nextZ)) {
                    continue;
                }
                int nextY = current.y + move.offsetY;
                if (nextY > ceiling || nextY < lowest) {
                    continue;
                }

                scratch.clear();
                expanders[move.ordinal()].expand(current.x, current.y, current.z, scratch);
                double stepCost = scratch.cost;
                if (stepCost >= dev.helm.pathfinding.cost.MoveCosts.IMPOSSIBLE) {
                    continue;
                }
                if (stepCost <= 0 || Double.isNaN(stepCost)) {
                    throw new IllegalStateException(move + " produced cost " + stepCost
                            + " from " + current.x + " " + current.y + " " + current.z);
                }
                if (move.offsetFollowsBlock && !world.insideBorder(scratch.x, scratch.z)) {
                    continue;
                }
                if (!move.offsetFollowsBlock
                        && (scratch.x != nextX || scratch.z != nextZ)) {
                    throw new IllegalStateException(move + " left the expected column");
                }
                if (!move.levelFollowsBlock && scratch.y != nextY) {
                    throw new IllegalStateException(move + " left the expected level");
                }

                Node neighbour = store.at(scratch.x, scratch.y, scratch.z, goal);
                if (neighbour.cost - (current.cost + stepCost) <= minImprovement) {
                    continue;
                }
                neighbour.adopt(current, stepCost);
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
            return SearchOutcome.CANCELLED;
        }
        return best.best(startX, startY, startZ) == null
                ? SearchOutcome.NO_PATH
                : SearchOutcome.PARTIAL_PATH;
    }

    private boolean crossesChunk(int fromX, int fromZ, int toX, int toZ) {
        return (toX >> 4) != (fromX >> 4) || (toZ >> 4) != (fromZ >> 4);
    }
}