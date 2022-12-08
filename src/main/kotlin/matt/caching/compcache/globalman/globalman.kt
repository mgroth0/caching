package matt.caching.compcache.globalman

import matt.caching.compcache.ComputeInput
import matt.caching.compcache.cache.ComputeCache
import matt.caching.compcache.cache.ComputeCacheBase
import matt.caching.compcache.cache.FakeComputeCache
import matt.caching.compcache.cache.HardComputeCache
import matt.collect.map.lazyMutableMap
import kotlin.reflect.KClass

val GlobalRAMComputeCacheManager = RAMComputeCacheManager()

interface ComputeCacheManager {
  fun cacheFactory(cls: KClass<ComputeInput<*>>): ComputeCacheBase<*, *>
  operator fun get(computeInput: ComputeInput<*>): ComputeCacheBase<*, *>
}

open class RAMComputeCacheManager: ComputeCacheManager {
  override operator fun get(computeInput: ComputeInput<*>) = computeCaches[computeInput::class]!!

  @PublishedApi internal val computeCaches =
	lazyMutableMap<KClass<ComputeInput<*>>, ComputeCacheBase<*, *>> { theClass ->
	  cacheFactory(theClass)
	}

  override fun cacheFactory(cls: KClass<ComputeInput<*>>): ComputeCacheBase<*, *> {
	return ComputeCache<ComputeInput<Any?>, Any?>(
	  cls
	).also {
	  it.isSetup = true
	}
  }

  /* @Suppress("UNCHECKED_CAST")
   inline fun <reified I: ComputeInput<*>> removeKeysWhere(condition: (I)->Boolean) {
	 val cls = I::class
	 val cache = computeCaches[cls as KClass<ComputeInput<*>>]
	 val ks = cache.computeCache.keys.toList()
	 ks.forEach {
	   if (condition(it as I)) {
		 cache.computeCache.remove(it)
	   }
	 }
   }*/

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


object FakeCacheManager: ComputeCacheManager {

  override fun cacheFactory(cls: KClass<ComputeInput<*>>): ComputeCacheBase<*, *> {
	return FakeComputeCache<ComputeInput<Any?>, Any?>(
	  /*cls*/
	)/*.also {
	  *//*it.isSetup = true*//*
	}*/
  }

  override fun get(computeInput: ComputeInput<*>): ComputeCacheBase<*, *> {
	return FakeComputeCache<ComputeInput<Any?>, Any?>(
	  /*cls*/
	)
  }
}