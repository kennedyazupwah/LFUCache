# LFU Cache Implementation
## Overview
This LFU (Least Frequently Used) Cache implementation is designed to automatically evict the least frequently accessed items when reaching its maximum capacity. The implementation is optimized for high-performance and concurrency, making it suitable for scenarios where fast access to frequently used items is critical.

## LFU Design
The cache employs an LFU eviction policy, distinguishing it from other caching strategies by focusing on access frequency rather than recency. This approach ensures that the most frequently accessed items are retained, optimizing cache hits for common access patterns.

## Interface Method Designs
The cache interface defines several key operations:

- get(K key): Retrieves an item from the cache.
- put(K key, V value): Adds or updates an item in the cache.
- remove(K key): Removes an item from the cache.
- clear(): Clears the cache.
- size(): Returns the number of items in the cache.

These methods provide a comprehensive API for interacting with the cache, covering most basic operations necessary for cache management.

#### Choice of ConcurrentHashMap
ConcurrentHashMap was chosen for storing cache entries due to its high concurrency level and thread-safety features. It allows concurrent read and write operations without the need for explicit synchronization, making it ideal for high-performance caching solutions where multiple threads may access the cache simultaneously.

#### Choice of DoublyLinkedList for Frequency Map Tracker
A DoublyLinkedList is used as the frequency map tracker to efficiently manage the order of cache entries based on their access frequencies. This data structure supports constant time insertions and deletions, which are essential for updating the frequency of cache entries dynamically. The choice of a doubly-linked list allows for quick movement of entries between frequency buckets, optimizing the performance of frequency-based eviction.

## Known Issues with the Current Implementation
- Lack of Linter: Currently, the project does not incorporate a linter tool, which could lead to inconsistent coding styles and unnoticed syntax errors. Integrating a linter would help enforce coding standards and improve code quality.
- Limited Testing: The existing test coverage might not fully capture the concurrency scenarios or the correctness of the eviction policy under edge cases. Expanding the test suite is necessary for ensuring the reliability of the cache implementation.
- Manual Expiration Handling: The expiration cleanup task is managed by a custom implementation, which may not be as efficient or reliable as using built-in concurrency utilities designed for such purposes.
