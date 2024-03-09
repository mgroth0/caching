package matt.caching.maybe

import matt.lang.model.value.Variable
import kotlin.time.Duration


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
    ): V = map.getOrPut(key, getter)
}

class NotACache<K, V : Any> : MaybeCache<K, V> {
    override fun getOrPut(
        key: K,
        getter: () -> V
    ): V = getter()
}

interface CacheFactory {
    fun <K, V : Any> createMaybeCache(): MaybeCache<K, V>
}

data object NoCacheFactory : CacheFactory {
    override fun <K, V : Any> createMaybeCache(): MaybeCache<K, V> = NotACache()
}

data object MapCacheFactory : CacheFactory {
    override fun <K, V : Any> createMaybeCache(): MaybeCache<K, V> = MapCache()
}


interface SuspendingMaybeCache<K, V : Any> {
    suspend fun getOrPut(
        key: K,
        getter: suspend () -> V
    ): V
}

interface SuspendingMaybeCache2<V : Any> {
    suspend fun getOrPut(
        getter: suspend () -> V
    ): V
}

interface SuspendingCacheFactory<T : Any> {
    val lifespan: Duration?
    suspend fun clear()
    fun <K, V : Any> createMaybeCache(subMapGetter: (T) -> MutableMap<K, V>): SuspendingMaybeCache<K, V>
    fun <V : Any> createMaybeCache2(subMapGetter: (T) -> Variable<V?>): SuspendingMaybeCache2<V>
}

interface ExpiringSuspendingCacheFactory<T : Any> : SuspendingCacheFactory<T> {
    override val lifespan: Duration
}

class MemorySuspendingCacheFactory<T : Any> : SuspendingCacheFactory<T> {
    override val lifespan = null
    override suspend fun clear() {
        TODO("Not yet implemented")
    }

    override fun <V : Any> createMaybeCache2(subMapGetter: (T) -> Variable<V?>): SuspendingMaybeCache2<V> {
        TODO("Not yet implemented")
    }

    override fun <K, V : Any> createMaybeCache(subMapGetter: (T) -> MutableMap<K, V>): SuspendingMaybeCache<K, V> {
        TODO("Not yet implemented")
    }
}
