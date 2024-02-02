package matt.caching.cache

class LRUCache<K, V>(private val cacheSize: Int) : LinkedHashMap<K, V>(16, 0.75f, true) {
    override fun removeEldestEntry(eldest: Map.Entry<K, V>) = size >= cacheSize
}
