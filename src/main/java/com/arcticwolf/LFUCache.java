package com.arcticwolf;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * An LFU (Least Frequently Used) cache implementation that automatically evicts the least frequently accessed items
 * when it reaches its maximum capacity. This cache is thread-safe and supports time-based expiration of cache entries.
 * <p>
 * The cache uses a {@link ConcurrentHashMap} for storing cache entries, ensuring thread-safe access and modifications.
 * A {@link ConcurrentSkipListMap} is used to track the frequencies of access for each cache entry, allowing efficient
 * retrieval and update of access frequencies. Expired entries are periodically cleaned up by a background thread,
 * which checks the timestamp of each entry against the configured expiration time.
 * <p>
 * Usage of this cache is suitable for scenarios where it's crucial to maintain quick access to frequently used items
 * while limiting memory usage to a predefined capacity. It's particularly useful in applications requiring high
 * performance and efficient memory use under concurrent access patterns.
 * @param <K> the type of keys maintained by this cache
 * @param <V> the type of mapped values used by the cache
 */
public class LFUCache<K,V> implements Cache<K,V> {
    private final int maxCacheCapacity;
    private final Map<K, CacheNode<K, V>> cache;
    private final ConcurrentSkipListMap<Integer, LFUDoublyLinkedListFrequencyTracker<K, V>> frequencyTrackerMap;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();


    public LFUCache(CacheConfig config) {
        if (config.getEntryExpirationTimeSeconds() <= 0 || config.getMaxCacheCapacity() <= 0) {
            throw new IllegalArgumentException("Positive Integers are Expected");
        }
        maxCacheCapacity = config.getMaxCacheCapacity();
        cache = new ConcurrentHashMap<>(maxCacheCapacity);
        frequencyTrackerMap = new ConcurrentSkipListMap<>();
        startCleanupTask();
    }

    /**
     * Retrieves the value associated with the specified key in this cache, or {@code null} if the cache contains no
     * mapping for the key or if the mapping has expired. Accessing an entry updates its access frequency.
     *
     * @param key the key whose value is to be returned
     * @return the value associated with the specified key, or {@code null} if no mapping exists or it has expired
     */
    public V get(K key) {
        lock.readLock().lock();
        try {
            CacheNode<K, V> entry = cache.get(key);
            if (entry == null) {
                return null;
            }
            updateFrequency(entry);
            return entry.getValue();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Associates the specified value with the specified key in this cache. If the cache previously contained a
     * mapping for the key, the old value is replaced. If adding a new entry exceeds the cache's capacity, the least
     * frequently used entry is evicted.
     *
     * @param key   the key with which the specified value is to be associated
     * @param value the value to be associated with the specified key
     */
    @Override
    public void put(K key, V value) {
        lock.writeLock().lock();
        try {
            CacheNode<K, V> entry = cache.get(key);
            if (entry != null) {
                entry.setValue(value);
                updateFrequency(entry);
            } else {
                if (cache.size() == maxCacheCapacity) {
                    removeLeastFrequent();
                }
                CacheNode<K, V> newEntry = new CacheNode<>(key, value, 1);
                cache.put(key, newEntry);
                updateFrequency(newEntry);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Removes the mapping for a key from this cache if it is present. The removed entry is also removed from the
     * frequency tracking.
     *
     * @param key the key whose mapping is to be removed from the cache
     */
    @Override
    public void remove(K key) {
        lock.writeLock().lock();
        try {
            CacheNode<K, V> entry = cache.remove(key);
            if (entry != null) {
                updateFrequency(entry);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Removes all of the mappings from this cache. The cache will be empty after this call returns, and all frequency
     * tracking data is also cleared.
     */
    @Override
    public void clear() {
        lock.writeLock().lock();
        try {
            cache.clear();
            frequencyTrackerMap.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public int size() {
        return cache.size();
    }

    /**
     * Updates the frequency of access for the specified cache entry. This method is called whenever an entry is accessed
     * (via {@code get} or {@code put}) to increment its frequency count. It manages the internal frequency tracking
     * structures to reflect the new access frequency of the entry.
     * <p>
     * When an entry's frequency is updated, it is moved to the appropriate position in the frequency tracking map to
     * maintain the order of entries by their frequencies. This ensures that the cache can efficiently identify and evict
     * the least frequently used entries when necessary.
     * <p>
     * This method is an integral part of the cache's mechanism for prioritizing entries based on their access patterns,
     * supporting the eviction policy that targets the least frequently accessed entries for removal when the cache
     * reaches its capacity.
     * <p>
     *
     * @param entry the cache entry whose frequency is to be updated
     */
    private void updateFrequency(CacheNode<K, V> entry) {

        lock.writeLock().lock();

        int currentFreq = entry.getAccessFrequency();
        LFUDoublyLinkedListFrequencyTracker<K, V> matchedItemsWithFrequency = frequencyTrackerMap.get(currentFreq);
        if (matchedItemsWithFrequency != null) {
            matchedItemsWithFrequency.removeEntry(entry);
            if (matchedItemsWithFrequency.isEmpty()) {
                frequencyTrackerMap.remove(currentFreq);
            }
        }

        // Increment frequency
        int newFreq = currentFreq + 1;
        entry.setAccessFrequency(newFreq);
        LFUDoublyLinkedListFrequencyTracker<K, V> updatedListWithNewFreq = frequencyTrackerMap.get(newFreq);
        if (updatedListWithNewFreq == null) {
            updatedListWithNewFreq = new LFUDoublyLinkedListFrequencyTracker<>();
            frequencyTrackerMap.put(newFreq, updatedListWithNewFreq);
        }
        updatedListWithNewFreq.addEntry(entry);

    }

    /**
     * Removes the least frequently accessed entry from the cache. If multiple entries have the same lowest frequency of
     * access, the least recently used among them is evicted. This method is typically invoked automatically when adding
     * a new entry to a full cache to maintain the cache's maximum capacity constraint.
     * <p>
     * This operation involves the following steps:
     * <ol>
     *     <li>Identify the least frequently accessed entries based on the frequency tracking data.</li>
     *     <li>If there are multiple entries with the same lowest frequency, identify the least recently used entry among them.</li>
     *     <li>Remove the identified entry from the cache and update the frequency tracking data accordingly.</li>
     * </ol>
     * <p>
     * Note: This method is thread-safe and ensures consistency between the cache and frequency tracking data even when
     * accessed by multiple threads concurrently.
     */
    private void removeLeastFrequent() {
        if (frequencyTrackerMap.isEmpty()) return;

        lock.writeLock().lock();
        try {
            Integer leastFrequency = frequencyTrackerMap.firstKey();
            LFUDoublyLinkedListFrequencyTracker<K, V> matchedFreqEntry = frequencyTrackerMap.get(leastFrequency);
            if (matchedFreqEntry != null && !matchedFreqEntry.isEmpty()) {
                CacheNode<K, V> toRemove = matchedFreqEntry.removeLRU();
                if (toRemove != null) {
                    cache.remove(toRemove.getKey());
                    if (matchedFreqEntry.isEmpty()) {
                        frequencyTrackerMap.remove(leastFrequency);
                    }
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Initializes and starts a background cleanup task to periodically remove expired entries from the cache. The task
     * runs at a fixed rate, specified by {@code CLEANUP_THREAD_PERIOD_MILLIS}, to check and evict entries that have
     * exceeded their specified time-to-live (TTL) since their last access.
     * <p>
     * The cleanup operation ensures that the cache does not retain stale data and helps maintain the cache's efficiency
     * and effectiveness over time. Expired entries are determined based on their timestamp and the cache's configured
     * {@code entryExpirationTimeMillis}.
     * <p>
     * This method creates a daemon thread to perform the cleanup task, ensuring that the thread does not prevent the JVM
     * from shutting down if the application is terminated. The cleanup task is intended to run for the lifetime of the
     * cache instance and automatically ceases operation when the cache is no longer in use or the application is closed.
     * <p>
     * Note: This method is typically called once during the cache initialization process and should not be invoked
     * multiple times to avoid creating multiple cleanup tasks.
     */
    private void startCleanupTask() {
        Thread expiryCleanupThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(CacheConfig.getCacheEntryExpiryTime());
                    long currentTime = System.currentTimeMillis();

                    // This part remove expired entries
                    cache.entrySet().removeIf(entry ->
                            currentTime - entry.getValue().getCreationTime() >= CacheConfig.getCacheEntryExpiryTime());

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        expiryCleanupThread.setDaemon(true);
        expiryCleanupThread.start();
    }
}
