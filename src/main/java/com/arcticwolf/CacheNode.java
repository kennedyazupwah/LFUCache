package com.arcticwolf;

public class CacheNode<K,V> {
    private final K key;
    private V value;
    private int accessFrequency;
    private final long creationTime;
    CacheNode<K, V> nextNode;
    CacheNode<K, V> previousNode;

    public CacheNode(K key, V value, int accessFrequency) {
        this.key = key;
        this.value = value;
        this.accessFrequency = accessFrequency;
        this.creationTime = System.currentTimeMillis();
    }

    public CacheNode<K, V> getNextNode() {
        return nextNode;
    }

    public void setNextNode(CacheNode<K, V> nextNode) {
        this.nextNode = nextNode;
    }

    public CacheNode<K, V> getPreviousNode() {
        return previousNode;
    }

    public void setPreviousNode(CacheNode<K, V> previousNode) {
        this.previousNode = previousNode;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }

    public int getAccessFrequency() {
        return accessFrequency;
    }

    public void setAccessFrequency(int accessFrequency) {
        this.accessFrequency = accessFrequency;
    }

    public long getCreationTime() {
        return creationTime;
    }
}
