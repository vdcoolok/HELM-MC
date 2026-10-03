package dev.helm.outline.block;

import java.util.ArrayList;
import java.util.List;

public final class BlockSilhouetteFeeds {

    private static final List<BlockSilhouetteFeed> FEEDS = new ArrayList<>();

    private BlockSilhouetteFeeds() {
    }

    public static void add(BlockSilhouetteFeed feed) {
        FEEDS.add(feed);
    }

    public static List<BlockSilhouetteFeed> live() {
        return FEEDS;
    }
}
