package matt.caching.test


import matt.caching.cache.LRUCache
import matt.test.Tests
import kotlin.test.Test
import kotlin.test.assertEquals

class CachingTests: Tests() {

    @Test
    fun lruCacheDoesRemove() {
        val size = 10
        val cache = LRUCache<String, Int>(10)
        repeat(size + 1) {
            cache[it.toString()] = it
        }
        assertEquals(cache.size, size - 1) /*needs space for quickly adding 1 more?*/
    }
}
