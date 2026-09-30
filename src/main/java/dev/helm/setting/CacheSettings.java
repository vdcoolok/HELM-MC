package dev.helm.setting;

public final class CacheSettings extends SettingSection {

    public static final String ENABLED = "cache.enabled";
    public static final String PREFER_LOADED = "cache.preferLoadedChunks";
    public static final String PRUNE_FROM_MEMORY = "cache.pruneFromMemory";
    public static final String QUEUE_LIMIT = "cache.queueLimit";
    public static final String EXPIRY_SECONDS = "cache.expirySeconds";
    public static final String REPACK_ON_BLOCK_CHANGE = "cache.repackOnBlockChange";

    public CacheSettings() {
        flag(ENABLED, "Cache chunks to disk",
                "Remember chunks you have seen so routes can cross terrain that is no "
                        + "longer loaded.", true);
        flag(PREFER_LOADED, "Prefer loaded chunks",
                "Read the world the game currently has, and only fall back to the cache. "
                        + "Turn off to search using the cache alone.", true);
        flag(PRUNE_FROM_MEMORY, "Release distant regions",
                "Drop cached regions that are far from you to save memory.", true);
        count(QUEUE_LIMIT, "Pack queue limit",
                "How many chunks may wait to be packed at once.", 2000, 0, 100000);
        count(EXPIRY_SECONDS, "Chunk expiry in seconds",
                "Forget cached chunks older than this. Below zero keeps them forever.", -1,
                -1, 31536000);
        flag(REPACK_ON_BLOCK_CHANGE, "Repack on block change",
                "Re-read a chunk when a block HELM tracks changes in it.", true);
    }

    public boolean enabled() {
        return on(ENABLED);
    }

    public boolean preferLoaded() {
        return on(PREFER_LOADED);
    }

    public boolean pruneFromMemory() {
        return on(PRUNE_FROM_MEMORY);
    }

    public int queueLimit() {
        return level(QUEUE_LIMIT);
    }

    public int expirySeconds() {
        return level(EXPIRY_SECONDS);
    }

    public boolean repackOnBlockChange() {
        return on(REPACK_ON_BLOCK_CHANGE);
    }
}
