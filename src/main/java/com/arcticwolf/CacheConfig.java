package com.arcticwolf;

/**
 * Configuration class for caching mechanisms.
 * Allows setting the entry expiration time in seconds and maximum cache capacity.
 */
public class CacheConfig {
    private final long entryExpirationTimeSeconds;
    private final int maxCacheCapacity;
    private final static int CACHE_ENTRY_EXPIRY_TIME = 300;

    /**
     * Constructs a new CacheConfig with specified settings.
     *
     * @param entryExpirationTimeSeconds the time in seconds after which a cache entry should expire.
     * @param maxCacheCapacity the maximum number of entries the cache can hold.
     */
    public CacheConfig(int entryExpirationTimeSeconds, int maxCacheCapacity) {
        this.entryExpirationTimeSeconds = entryExpirationTimeSeconds;
        this.maxCacheCapacity = maxCacheCapacity;
    }

    public long getEntryExpirationTimeSeconds() {
        return entryExpirationTimeSeconds;
    }

    public int getMaxCacheCapacity() {
        return maxCacheCapacity;
    }

    /**
     * Returns the default expiration time for cache entries.
     *
     * @return the default expiration time in seconds.
     */
    public static int getCacheEntryExpiryTime(){
        return CACHE_ENTRY_EXPIRY_TIME;
    }
}
