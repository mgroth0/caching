package matt.caching.compcache.cache

import matt.async.safe.mutSemMapOf
import matt.caching.compcache.ComputeInputLike
import kotlin.reflect.KClass
import kotlin.reflect.cast

const val MAX_CACHE_SIZE = 1_000_000

open class ComputeCache<I : ComputeInputLike<O>, O: Any>(
    protected val inputCls: KClass<ComputeInputLike<*>>,
    final override var enableCache: Boolean = true
) : ComputeCacheBase<I, O>() {
    val computeCache = mutSemMapOf<Any, Any>(maxsize = MAX_CACHE_SIZE)
    /*final override operator fun get(input: I): O? = computeCache[input]*/
    final override fun get2(input: Any): Any? = computeCache[input]

    final override operator fun set(
        input: I,
        output: O
    ) {
        computeCache[input] = output
    }

    final override fun setIfNotFull(
        input: I,
        output: O
    ) = computeCache.setIfNotFull(input, output)
    final override fun setIfNotFull2(
        input: Any,
        output: Any
    ) = computeCache.setIfNotFull(inputCls.cast(input), output)
}


class FakeComputeCache<I : ComputeInputLike<O>, O>() : ComputeCacheBase<I, O>() {
    /*override fun get(input: I) = null*/
    override fun get2(input: Any): O? = null
    override fun set(
        input: I,
        output: O
    ) = Unit

    override fun setIfNotFull2(
        input: Any,
        output: Any
    ): Boolean = false

    override var enableCache = true
    override fun setIfNotFull(
        input: I,
        output: O
    ) = false
}


abstract class ComputeCacheBase<I : ComputeInputLike<O>, O> {
    abstract var enableCache: Boolean
    /*abstract operator fun get(input: I): O?*/
    abstract fun get2(input: Any): Any?
    abstract operator fun set(
        input: I,
        output: O
    )

    var full = false
    abstract fun setIfNotFull(
        input: I,
        output: O
    ): Boolean
    abstract fun setIfNotFull2(
        input: Any,
        output: Any
    ): Boolean

    internal var isSetup = false
}
