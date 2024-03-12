package com.arcticwolf;

public class LFUDoublyLinkedListCacheList<K,V> {
    private int count;
    private CacheNode<K, V> first;
    private CacheNode<K, V> last;

    public boolean isEmpty() {
        return count == 0;
    }

    public void addEntry(CacheNode<K, V> entry) {
        if (first == null) {
            first = last = entry;
        } else {
            last.setNextNode(entry);
            entry.setPreviousNode(last);
            last = entry;
        }
        count++;
    }

    public void removeEntry(CacheNode<K, V> entry) {
        if (entry.getPreviousNode() == null) {
            first = entry.getNextNode();
        } else {
            entry.getPreviousNode().setNextNode(entry.getNextNode());
        }

        if (entry.getNextNode() == null) {
            last = entry.getPreviousNode();
        } else {
            entry.getNextNode().setPreviousNode(entry.getPreviousNode());
        }

        count--;
    }
    public CacheNode<K, V> removeLRU() {
        if (first == null) {
            return null;
        }

        CacheNode<K, V> lru = first;
        removeEntry(lru);
        return lru;
    }
}
