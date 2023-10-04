package matt.caching.maybe


interface MaybeCache<K, V : Any> {
    fun getOrPut(
        key: K,
        getter: () -> V
    ): V
}


class MapCache<K, V : Any> private constructor(private val map: MutableMap<K, V> = mutableMapOf()) : MaybeCache<K, V> {
    constructor() : this(mutableMapOf())

    @Synchronized
    override fun getOrPut(
        key: K,
        getter: () -> V
    ): V {
        return map.getOrPut(key, getter)
    }
}

class NotACache<K, V : Any> : MaybeCache<K, V> {
    override fun getOrPut(
        key: K,
        getter: () -> V
    ): V {
        return getter()
    }
}

interface CacheFactory {
    fun <K, V : Any> createMaybeCache(): MaybeCache<K, V>
}

data object NoCacheFactory : CacheFactory {
    override fun <K, V : Any> createMaybeCache(): MaybeCache<K, V> {
        return NotACache()
    }

}

data object MapCacheFactory : CacheFactory {
    override fun <K, V : Any> createMaybeCache(): MaybeCache<K, V> {
        return MapCache()
    }
}


interface SuspendingMaybeCache<K, V : Any> {
    suspend fun getOrPut(
        key: K,
        getter: () -> V
    ): V
}

interface SuspendingCacheFactory<T : Any> {
    val lifespan: kotlin.time.Duration
    suspend fun clear()
    fun <K, V : Any> createMaybeCache(subMapGetter: (T) -> MutableMap<K, V>): SuspendingMaybeCache<K, V>
}

