package matt.caching.compcache.globalman

import matt.caching.compcache.ComputeInput
import matt.caching.compcache.cache.ComputeCache
import matt.caching.compcache.cache.HardComputeCache
import matt.collect.map.lazyMutableMap
import kotlin.reflect.KClass

val GlobalRAMComputeCacheManager = RAMComputeCacheManager()


open class RAMComputeCacheManager {
  operator fun get(computeInput: ComputeInput<*>) = computeCaches[computeInput::class]!!

  @PublishedApi internal val computeCaches = lazyMutableMap<KClass<ComputeInput<*>>, ComputeCache<*, *>> { theClass ->
	cacheFactory(theClass)
  }

  open fun cacheFactory(cls: KClass<ComputeInput<*>>): ComputeCache<*, *> {
	return ComputeCache<ComputeInput<Any?>, Any?>(
	  cls
	).also {
	  it.isSetup = true
	}
  }

  @Suppress("UNCHECKED_CAST")
  inline fun <reified I: ComputeInput<*>> removeKeysWhere(condition: (I)->Boolean) {
	val cls = I::class
	val cache = computeCaches[cls as KClass<ComputeInput<*>>]
	val ks = cache.computeCache.keys.toList()
	ks.forEach {
	  if (condition(it as I)) {
		cache.computeCache.remove(it)
	  }
	}
  }

}


class HardStorageCacheManager: RAMComputeCacheManager() {

  override fun cacheFactory(cls: KClass<ComputeInput<*>>): ComputeCache<*, *> {
	return HardComputeCache<ComputeInput<Any?>, Any?>(
	  cls
	).also {
	  it.isSetup = true
	}
  }
}