package matt.caching.compcache.globalman

import matt.caching.compcache.ComputeCacheContext
import matt.caching.compcache.ComputeInputLike
import matt.caching.compcache.cache.ComputeCache
import matt.caching.compcache.cache.ComputeCacheBase
import matt.caching.compcache.cache.FakeComputeCache
import matt.caching.compcache.cache.HardComputeCache
import matt.collect.map.lazyMutableMap
import kotlin.reflect.KClass

//val GlobalRAMComputeCacheManager = RAMComputeCacheManager()

interface ComputeCacheManager {
    fun cacheFactory(cls: KClass<ComputeInputLike<*>>): ComputeCacheBase<*, *>
    operator fun get(computeInput: ComputeInputLike<*>): ComputeCacheBase<*, *>
}

open class RAMComputeCacheManager : ComputeCacheManager {
    override operator fun get(computeInput: ComputeInputLike<*>) = computeCaches[computeInput::class]!!

    @PublishedApi
    internal val computeCaches =
        lazyMutableMap<KClass<ComputeInputLike<*>>, ComputeCacheBase<*, *>> { theClass ->
            cacheFactory(theClass)
        }

    override fun cacheFactory(cls: KClass<ComputeInputLike<*>>): ComputeCacheBase<*, *> {
        return ComputeCache<ComputeInputLike<Any?>, Any?>(
            cls
        ).also {
            it.isSetup = true
        }
    }
}


class HardStorageCacheManager : RAMComputeCacheManager() {

    override fun cacheFactory(cls: KClass<ComputeInputLike<*>>): ComputeCache<*, *> {
        return HardComputeCache<ComputeInputLike<Any?>, Any?>(
            cls
        ).also {
            it.isSetup = true
        }
    }
}


object FakeCacheManager : ComputeCacheManager, ComputeCacheContext {

    override fun cacheFactory(cls: KClass<ComputeInputLike<*>>): ComputeCacheBase<*, *> {
        return FakeComputeCache<ComputeInputLike<Any?>, Any?>()
    }

    override fun get(computeInput: ComputeInputLike<*>): ComputeCacheBase<*, *> {
        return FakeComputeCache<ComputeInputLike<Any?>, Any?>()
    }

    override val cacheManager = this
}