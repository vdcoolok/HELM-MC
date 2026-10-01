package dev.helm.navigate;

import dev.helm.pathfinding.context.Tunables;
import dev.helm.pathfinding.context.TunablesFactory;
import dev.helm.pathfinding.goal.Goal;
import dev.helm.pathfinding.move.MoveEnvironment;
import dev.helm.pathfinding.move.MoveExpander;
import dev.helm.pathfinding.move.MoveKind;
import dev.helm.pathfinding.move.expand.Expanders;
import dev.helm.pathfinding.search.Search;
import dev.helm.pathfinding.search.SearchBudget;
import dev.helm.pathfinding.search.SearchCoordinator;
import dev.helm.pathfinding.search.SearchJob;
import dev.helm.pathfinding.world.BlockView;
import dev.helm.pathfinding.world.block.WalkRules;
import dev.helm.pathfinding.world.block.WorkCosts;
import dev.helm.setting.MovementSettings;
import dev.helm.setting.PathSettings;
import dev.helm.setting.Settings;
import dev.helm.tools.BlockAvoidList;
import dev.helm.tools.BreakStrength;
import dev.helm.tools.MinedToolStrength;
import dev.helm.world.ClientLevelView;
import dev.helm.world.PlayerInventory;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.block.state.BlockState;

public final class Navigator implements MoveEnvironment {

    private final MoveExpander[] expanders = new MoveExpander[MoveKind.values().length];
    private final SearchCoordinator searches = new SearchCoordinator();

    private WalkRules walk;
    private WorkCosts work;
    private ClientLevelView view;
    private java.util.Set<net.minecraft.world.level.block.Block> doNotBreak;
    private BreakStrength strength;
    private Tunables tuning;
    private boolean ready;

    public Navigator() {
        refresh();
    }

    public void refresh() {
        Minecraft client = Minecraft.getInstance();
        ClientLevel level = client.level;
        LocalPlayer player = client.player;
        if (level == null || player == null) {
            ready = false;
            return;
        }
        MovementSettings movement = Settings.holder().movement();
        tuning = TunablesFactory.from(movement);
        doNotBreak = BlockAvoidList.all();
        view = new ClientLevelView(level, doNotBreak);
        walk = new WalkRules(view, tuning);
        strength = new MinedToolStrength(new PlayerInventory(player), Settings.holder().mining(), movement);
        work = new WorkCosts(view, walk, tuning, strength);
        MoveExpander[] built = Expanders.forEnvironment(this);
        System.arraycopy(built, 0, expanders, 0, built.length);
        ready = true;
    }

    public boolean ready() {
        return ready;
    }

    public WalkRules walk() {
        return walk;
    }

    public WorkCosts work() {
        return work;
    }

    public Tunables tuning() {
        return tuning;
    }

    public ClientLevelView view() {
        return view;
    }

    public BlockView blocks() {
        return view;
    }

    public MoveExpander[] expanders() {
        return expanders;
    }

    @Override
    public BlockState stateAt(int x, int y, int z) {
        return view.stateAt(x, y, z);
    }

    @Override
    public boolean loaded(int x, int z) {
        return view.loaded(x, z);
    }

    @Override
    public boolean residentChunk(int x, int z) {
        return view.residentChunk(x, z);
    }

    @Override
    public int lowestLevel() {
        return view.lowestLevel();
    }

    @Override
    public int levelCount() {
        return view.levelCount();
    }

    @Override
    public boolean entirelyInsideBorder(int x, int z) {
        return view.entirelyInsideBorder(x, z);
    }

    @Override
    public boolean canPlaceAt(int x, int z) {
        return view.canPlaceAt(x, z);
    }

    public Search searchTo(Goal goal, int fromX, int fromY, int fromZ) {
        PathSettings path = Settings.holder().path();
        double improvement = path.repropagateImprovement()
                ? SearchBudget.MIN_IMPROVEMENT
                : 0;
        return new Search(fromX, fromY, fromZ, goal, view, expanders,
                new SearchBudget(path.primaryTimeoutMillis(), path.failureTimeoutMillis(),
                        path.maxChunkBorderFetch(), improvement));
    }

    public SearchJob searchFor(Goal goal, int fromX, int fromY, int fromZ) {
        return searches.submit(goal, fromX, fromY, fromZ, doNotBreak);
    }

    public boolean cancelSearch() {
        return searches.cancel();
    }

    public void shutdown() {
        searches.shutdown();
    }
}
