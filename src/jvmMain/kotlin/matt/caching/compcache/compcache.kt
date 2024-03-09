package matt.caching.compcache

import matt.caching.compcache.cache.ComputeCacheBase
import matt.caching.compcache.globalman.ComputeCacheManager
import matt.caching.compcache.globalman.FakeCacheManager
import matt.caching.compcache.globalman.RAMComputeCacheManager
import matt.lang.common.go

/*
abstract class HardStorageComputeInput<O> : ComputeInput<O>() {

    abstract override val cacheManager: HardStorageCacheManager

    val stupidHardCache get() = cache as HardComputeCache<ComputeInput<O>, O>

    private val cacheFile by lazy { stupidHardCache.cacheFile }

    private fun loadCache(): ComputeCache<ComputeInput<O>, O> {
        val s = cacheFile.text
        return Json.decodeFromString(s)
    }

    context (ComputeCacheContext)
    fun maybeLoad() {
        val c = cache()
        if (!c.isSetup && cacheFile.exists() && !cacheFile.isBlank()) {
            stupidHardCache.load()
            c.isSetup = true
        }
    }

    context (ComputeCacheContext)
    override fun preFindOrCompute() {
        maybeLoad()
    }

}*/


abstract class UpdaterComputeInput<K, V> : ComputeInput<Map<K, V>, ComputeCacheContext>() {
    context(ComputeCacheContext)
    abstract fun futureMapBuilder(): Map<K, V>

    context(ComputeCacheContext)
    final override fun compute() = compute { }

    context(ComputeCacheContext)
    inline fun compute(op: (Int) -> Unit): Map<K, V> {
        val fm = futureMapBuilder()
        return fm
    }

    context (ComputeCacheContext)
    operator fun invoke(inPlaceUpdateOp: ((Int) -> Unit)) = findOrCompute(inPlaceUpdateOp)

    context (ComputeCacheContext)
    inline fun findOrCompute(inPlaceUpdateOp: ((Int) -> Unit)): Map<K, V> {
        val c = cache()
        return if (!c.enableCache) {
            compute(inPlaceUpdateOp)
        } else run {
            c[this] ?: compute(inPlaceUpdateOp).also {
                if (!c.full) {
                    c.full = !c.setIfNotFull(this, it)
                }
            }
        }
    }
}



interface ComputeCacheContext {
    val cacheManager: ComputeCacheManager
}


data class ComputeCacheContextImpl(override val cacheManager: ComputeCacheManager = RAMComputeCacheManager()) :
    ComputeCacheContext

abstract class FakeCacheComputeInput<O> : ComputeInput<O, FakeCacheManager>()
abstract class GenericComputeInput<O> : ComputeInput<O, ComputeCacheContext>()

sealed interface ComputeInputLike<O>

abstract class ComputeInput<O, CCC : ComputeCacheContext> : ComputeInputLike<O> {



    private var _cache: ComputeCacheBase<ComputeInput<O, *>, O>? = null


    context (CCC)
    @PublishedApi
    internal fun cache(): ComputeCacheBase<ComputeInput<O, *>, O> {
        _cache?.go { return it }
        synchronized(this) {
            _cache?.go { return it }
            @Suppress("UNCHECKED_CAST", "UNCHECKED_CAST")
            (cacheManager[this] as ComputeCacheBase<ComputeInput<O, *>, O>).go {
                _cache = it
                return it
            }
        }
    }

    context (CCC)
    abstract fun compute(): O

    context (CCC)
    operator fun invoke() = findOrCompute()

    internal open fun preFindOrCompute() {}

    context (CCC)
    fun findOrCompute(): O {

        preFindOrCompute()
        val c = cache()
        return if (!c.enableCache) {
            compute()
        } else run {
            c[this] ?: compute().also {
                if (!c.full) {
                    c.full = !c.setIfNotFull(this, it)
                }
            }
        }
    }
}

abstract class GenericSuspendingComputeInput<O> : SuspendingComputeInput<O, ComputeCacheContext>()

abstract class SuspendingComputeInput<O, CCC : ComputeCacheContext> : ComputeInputLike<O> {

    private var _cache: ComputeCacheBase<SuspendingComputeInput<O, *>, O>? = null


    context (CCC)
    @PublishedApi
    internal fun cache(): ComputeCacheBase<SuspendingComputeInput<O, *>, O> {
        _cache?.go { return it }
        synchronized(this) {
            _cache?.go { return it }
            @Suppress("UNCHECKED_CAST", "UNCHECKED_CAST")
            (cacheManager[this] as ComputeCacheBase<SuspendingComputeInput<O, *>, O>).go {
                _cache = it
                return it
            }
        }
    }

    context (CCC)
    abstract suspend fun compute(): O

    context (CCC)
    suspend operator fun invoke() = findOrCompute()

    internal open fun preFindOrCompute() {}

    context (CCC)
    suspend fun findOrCompute(): O {

        preFindOrCompute()
        val c = cache()
        return if (!c.enableCache) {
            compute()
        } else run {


            c[this] ?: compute().also {
                if (!c.full) {
                    c.full = !c.setIfNotFull(this, it)
                }
            }
        }
    }
}

