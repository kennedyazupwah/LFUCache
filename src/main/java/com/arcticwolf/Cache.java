package com.arcticwolf;

/**
 * The Cache interface defines the operations for a cache system that stores key-value pairs. Implementations of this
 * interface are expected to provide mechanisms for adding, retrieving, removing, and managing entries based on specific
 * cache policies (e.g., eviction strategies, expiration policies).
 *
 * @param <K> the type of keys maintained by this cache
 * @param <V> the type of mapped values
 */
public interface Cache<K,V> {
    /**
     * Retrieves the value associated with the specified key from the cache. If the cache contains no mapping for
     * the key, returns {@code null} or optionally throws an exception, depending on the implementation.
     *
     * @param key the key whose associated value is to be returned
     * @return the value to which the specified key is mapped, or {@code null} if this cache contains no mapping for
     *         the key
     */
    V get(K key);

    /**
     * Associates the specified value with the specified key in the cache. If the cache previously contained a mapping
     * for the key, the old value is replaced by the specified value. Implementations may automatically evict entries
     * to make space for new ones based on the cache's policies.
     *
     * @param key   the key with which the specified value is to be associated
     * @param value the value to be associated with the specified key
     */
    void put(K key, V value);

    /**
     * Removes the mapping for a key from the cache if it is present. The method does nothing if the cache contains
     * no mapping for this key.
     *
     * @param key the key whose mapping is to be removed from the cache
     */
    void remove(K key);

    /**
     * Removes all mappings from the cache. The cache will be empty after this call returns.
     */
    void clear();

    /**
     * Returns the number of key-value mappings in the cache. This method allows users to check the size of the cache
     * at any given time.
     *
     * @return the number of key-value mappings in the cache
     */
    int size();
}
