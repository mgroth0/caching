package matt.caching.compcache

import matt.caching.compcache.cache.ComputeCacheBase
import matt.caching.compcache.globalman.ComputeCacheManager
import matt.caching.compcache.globalman.FakeCacheManager
import matt.caching.compcache.globalman.RAMComputeCacheManager
import matt.lang.cast.Caster
import matt.lang.common.go
import kotlin.reflect.KClass
import kotlin.reflect.cast


interface ComputeCacheContext {
    val cacheManager: ComputeCacheManager
}


data class ComputeCacheContextImpl(override val cacheManager: ComputeCacheManager = RAMComputeCacheManager()) :
    ComputeCacheContext

abstract class FakeCacheComputeInput<O> : ComputeInput<O, FakeCacheManager>()
abstract class GenericComputeInput<O> : ComputeInput<O, ComputeCacheContext>()

sealed interface ComputeInputLike<O>

abstract class ComputeInput<O, CCC : ComputeCacheContext> : ComputeInputLike<O> {



    @PublishedApi
    internal var _cache2: ComputeCacheBase<*, *>? = null


    context (CCC)
    @PublishedApi
    internal fun cache(): ComputeCacheBase<*, *> {
        _cache2?.go { return it }
        synchronized(this) {
            _cache2?.go { return it }
            val got = cacheManager[this]
            _cache2 = got
            return got
          /*  (cacheManager[this] as ComputeCacheBase<ComputeInput<O, *>, O>).go {

                _cache = it
                return it
            }*/
        }
    }

    context (CCC)
    abstract fun compute(): O


    @PublishedApi
    internal open fun preFindOrCompute() {}
}

context (CCC)
inline operator fun <reified O: Any, CCC, I: ComputeInput<out O, CCC>> I.invoke() = findOrCompute()



context (CCC)
inline fun <reified O: Any, CCC, I: ComputeInput<out O, CCC>> I.findOrCompute(): O =
    findOrCompute {
        it as O
    }

context (CCC)
fun <O: Any, CCC, I: ComputeInput<out O, CCC>> I.findOrCompute(cls: KClass<O>): O =
    findOrCompute {
        cls.cast(it)
    }

context (CCC)
fun <O: Any, CCC, I: ComputeInput<out O, CCC>> I.findOrCompute(caster: Caster<O>): O =
    findOrCompute {
        caster.cast(it)
    }

context (CCC)
fun <O: Any, CCC, I: ComputeInput<out O, CCC>> I.findOrCompute(cast: (Any?) -> O): O {
    preFindOrCompute()
    val c = cache()
    return if (!c.enableCache) {
        compute()
    } else run {
        val c2 = _cache2!!
        val r =
            c2.get2(this) ?: compute().also {
                if (!c2.full) {
                    c2.full = !c2.setIfNotFull2(this, it)
                }
            }
        cast(r)
        /*c[this] ?: compute().also {
            if (!c.full) {
                c.full = !c.setIfNotFull(this, it)
            }
        }*/
    }
}

/*abstract class SuspendingComputeInput<O, CCC : ComputeCacheContext> : ComputeInputLike<O> {

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
}*/

