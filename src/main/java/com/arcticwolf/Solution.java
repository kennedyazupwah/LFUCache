package com.arcticwolf;

import java.util.Objects;

public class Solution {
    public static void main(String[] args) {
        testPutAndGetOperations();
        testKeyNotPresent();
        testLFUEviction();
        testRemoveMethod();
        testClearMethod();
        testLFUEvictionWithLRUTieBreaker();
        testEntryExpirationFeature();
        testSizeMethod();

    }

    private static void assertEquals(Object actual, Object expected) {
        if (!Objects.equals(actual, expected)) {
            throw new IllegalStateException(String.format("Expected='%s; actual='%s'", expected, actual));
        }
    }

    private static void testPutAndGetOperations() {
        System.out.println("Starting Unit testPutAndGetOperations()...");
        CacheConfig config = new CacheConfig(10,10);

        LFUCache<String, Integer> intCache = new LFUCache<>(config);
        LFUCache<String, String> stringCache = new LFUCache<>(config);
        LFUCache<String, Person> personCache = new LFUCache<>(config);

        try {
            // Test with integers
            intCache.put("one", 1);
            intCache.put("two", 2);

            assertEquals(intCache.get("one"), 1);
            assertEquals(intCache.get("two"), 2);

            // Test with strings
            stringCache.put("hello", "World");
            stringCache.put("foo", "Bar");
            assertEquals(stringCache.get("hello"), "World");
            assertEquals(stringCache.get("foo"), "Bar");

            // Test with custom objects
            Person person1 = new Person("John Doe", 30);
            Person person2 = new Person("Jane Doe", 25);
            personCache.put("person1", person1);
            personCache.put("person2", person2);
            assertEquals(personCache.get("person1"), person1);
            assertEquals(personCache.get("person2"), person2);

            System.out.println("Test SUCCEEDED");
        } catch (IllegalStateException e) {
            System.out.printf("Test FAILED: %s%n", e.getMessage());
        }
    }

    private static void testKeyNotPresent() {
        System.out.println("Running Unit testKeyNotPresent()...");
        CacheConfig config = new CacheConfig(10,10);

        LFUCache<String, Integer> intCache = new LFUCache<>(config);

        // Pre-populate the cache with a different key
        intCache.put("existingKey", 42);

        // Attempt to retrieve a value for a non-existing key
        try {
            Integer value = intCache.get("nonExistingKey");
            assertEquals(value, null);

            System.out.println("Test SUCCEEDED: Correctly handled non-existing key.");
        } catch (IllegalStateException e) {
            System.out.printf("Test FAILED: %s%n", e.getMessage());
        }
    }

    public static void testRemoveMethod() {
        System.out.println("Running Unit testRemoveMethod()...");
        CacheConfig config = new CacheConfig(10,10);

        LFUCache<String, Integer> cache = new LFUCache<>(config);
        String testKey = "testKey";
        Integer testValue = 42;

        cache.put(testKey, testValue);

        cache.remove(testKey);

        if (cache.get(testKey) == null) {
            System.out.println("Test PASSED: The entry was successfully removed.");
        } else {
            System.out.println("Test FAILED: The entry was not removed.");
        }
    }

    public static void testClearMethod() {
        System.out.println("Running Unit testClearMethod()...");
        CacheConfig config = new CacheConfig(10,10);

        LFUCache<String, Integer> cache = new LFUCache<>(config);

        cache.put("key1", 1);
        cache.put("key2", 2);
        cache.put("key3", 3);

        cache.clear();

        boolean isCacheCleared = cache.get("key1") == null && cache.get("key2") == null && cache.get("key3") == null;
        if (isCacheCleared) {
            System.out.println("Test PASSED: The cache was successfully cleared.");
        } else {
            System.out.println("Test FAILED: The cache was not properly cleared.");
        }
    }

    private static void testLFUEviction() {
        System.out.println("Running Unit testLFUEviction()...");
        CacheConfig config = new CacheConfig(10,3);

        LFUCache<String, Integer> cache = new LFUCache<>(config);

        try {
            // Inserting three entries into the cache.
            cache.put("key1", 1);
            cache.put("key2", 2);
            cache.put("key3", 3);

            // 'key2' and 'key3' are now more frequently used than 'key1'
            cache.get("key2");
            cache.get("key3");

            // Adding another entry to force an eviction.
            cache.put("key4", 4);

            // Attempt to retrieve the entry expected to have been evicted.
            Integer evictedValue = cache.get("key1");

            assertEquals(evictedValue, null);

            System.out.println("Test SUCCEEDED: Correct LFU entry eviction.");
        } catch (IllegalStateException e) {
            System.out.printf("Test FAILED: %s%n", e.getMessage());
        }
    }

    private static void testLFUEvictionWithLRUTieBreaker() {
        System.out.println("Running Unit testLFUEvictionWithLRUTieBreaker()...");
        CacheConfig config = new CacheConfig(10,3);

        LFUCache<String, Integer> cache = new LFUCache<>(config);

        try {
            cache.put("key1", 1);
            cache.put("key2", 2);
            cache.put("key3", 3);

            // Access some entries to change their frequency and last access times.
            cache.get("key1");
            cache.get("key3");
            cache.get("key3");

            // Add another entry to trigger eviction.
            cache.put("key4", 4);

            // Attempt to access the entries expected to be evicted and not evicted.
            Integer valueEvicted = cache.get("key2");
            Integer valueNotEvicted = cache.get("key1");

            // Check if key2 was evicted and key1 was not.
            assertEquals(valueEvicted, null);
            assertEquals(valueNotEvicted, 1);

            System.out.println("Test SUCCEEDED: LFU eviction with LRU tie-breaker is working as expected.");
        } catch (Exception e) {
            System.out.printf("Test FAILED: %s%n", e.getMessage());
        }
    }

    private static void testEntryExpirationFeature() {
        System.out.println("Running Unit testEntryExpirationFeature...");
        CacheConfig config = new CacheConfig(10,1);

        final int entryExpirationTime = 2000;

        LFUCache<String, Integer> cache = new LFUCache<>(config);

        cache.put("key1", 1);

        try {
            // Simulate waiting to allow the entry to expire
            Thread.sleep(entryExpirationTime + 100);

            // Now check if the entry has expired
            if (cache.get("key1") == null) {
                System.out.println("Test PASSED: The entry has been correctly expired and removed.");
            } else {
                System.out.println("Test FAILED: The entry was not removed after its expiration time.");
            }
        } catch (InterruptedException e) {
            System.out.println("Test FAILED due to interruption: " + e.getMessage());
        }
    }

    public static void testSizeMethod() {
        System.out.println("Running Unit testSizeMethod()...");
        CacheConfig config = new CacheConfig(10,3);

        LFUCache<String, Integer> cache = new LFUCache<>(config);

        // Add entries
        cache.put("key1", 1);
        cache.put("key2", 2);
        cache.put("key3", 3);

        // Test size after adding entries
        if (cache.size() == 3) {
            System.out.println("Test PASSED: Correct size after adding entries.");
        } else {
            System.out.println("Test FAILED: Incorrect size after adding entries.");
        }

        // Remove an entry
        cache.remove("key2");

        // Test size after removing an entry
        if (cache.size() == 2) {
            System.out.println("Test PASSED: Correct size after removing an entry.");
        } else {
            System.out.println("Test FAILED: Incorrect size after removing an entry.");
        }

        // Clear the cache
        cache.clear();

        // Test size after clearing the cache
        if (cache.size() == 0) {
            System.out.println("Test PASSED: Correct size after clearing the cache.");
        } else {
            System.out.println("Test FAILED: Incorrect size after clearing the cache.");
        }
    }

}
