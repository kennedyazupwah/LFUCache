package com.arcticwolf;

public class CacheConfig {
    private final long entryExpirationTimeSeconds;
    private final int maxCacheCapacity;
    private final static int EXPIRY_TIME = 1;

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
    public static int getExpiryTime(){
        return EXPIRY_TIME;
    }
}
