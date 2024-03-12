package com.arcticwolf;

/**
 * Represents a doubly linked list specifically designed for use in LFU (Least Frequently Used) caching mechanisms.
 * This class manages the cache nodes, allowing for efficient addition and removal operations essential for LFU cache.
 *
 * @param <K> the type of keys maintained by this cache list
 * @param <V> the type of mapped values
 */
public class LFUDoublyLinkedListCacheList<K,V> {
    private int count;
    private CacheNode<K, V> first;
    private CacheNode<K, V> last;

    /**
     * Checks if the list is empty.
     *
     * @return true if the list contains no items, false otherwise.
     */
    public boolean isEmpty() {
        return count == 0;
    }

    /**
     * Adds a new entry to the end of the list. This method is used to add new cache nodes to the LFU cache.
     *
     * @param entry the cache node to be added to the list
     */
    public void addEntry(CacheNode<K, V> entry) {
        if (entry == null) return;

        if (isEmpty()) {
            first = last = entry;
        } else {
            entry.setPreviousNode(last);
            last.setNextNode(entry);
            last = entry;
        }
        count++;
    }

    /**
     * Removes the specified entry from the list. If the entry is not part of the list, the method does nothing.
     *
     * @param entry the cache node to be removed from the list
     */
    public void removeEntry(CacheNode<K, V> entry) {
        if (entry == null || isEmpty()) return;

        if (entry == first) {
            first = entry.getNextNode();
        } else {
            entry.getPreviousNode().setNextNode(entry.getNextNode());
        }

        if (entry == last) {
            last = entry.getPreviousNode();
        } else {
            entry.getNextNode().setPreviousNode(entry.getPreviousNode());
        }

        count--;
    }

    /**
     * Removes and returns the least recently used (LRU) cache node from the list, which is the first node.
     *
     * @return the LRU cache node, or null if the list is empty.
     */
    public CacheNode<K, V> removeLRU() {
        if (first == null) {
            return null;
        }

        CacheNode<K, V> lru = first;
        removeEntry(lru);
        return lru;
    }
}
