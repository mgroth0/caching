package matt.caching.compcache

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import matt.async.par.FutureMap
import matt.caching.compcache.cache.ComputeCache
import matt.caching.compcache.cache.ComputeCacheBase
import matt.caching.compcache.cache.HardComputeCache
import matt.caching.compcache.globalman.ComputeCacheManager
import matt.caching.compcache.globalman.GlobalRAMComputeCacheManager
import matt.caching.compcache.globalman.HardStorageCacheManager


abstract class HardStorageComputeInput<O>: GlobalRAMComputeInput<O>() {

  abstract override val cacheManager: HardStorageCacheManager

  val stupidHardCache get() = cache as HardComputeCache<ComputeInput<O>, O>

  private val cacheFile by lazy { stupidHardCache.cacheFile }

  private fun loadCache(): ComputeCache<GlobalRAMComputeInput<O>, O> {
	val s = cacheFile.text
	return Json.decodeFromString(s)
  }

  fun maybeLoad() {
	if (!cache.isSetup && cacheFile.exists() && !cacheFile.isBlank()) {
	  stupidHardCache.load()
	  cache.isSetup = true
	}
  }


  override fun preFindOrCompute() {
	maybeLoad()
  }

}


abstract class UpdaterComputeInput<K, V>: GlobalRAMComputeInput<Map<K, V>>() {
  abstract fun futureMapBuilder(): FutureMap<K, V>
  override fun compute() = compute { }
  inline fun compute(op: (Int)->Unit): Map<K, V> {
	val fm = futureMapBuilder()
	fm.fill(op)
	return fm.map
  }

  operator fun invoke(inPlaceUpdateOp: ((Int)->Unit)) = findOrCompute(inPlaceUpdateOp)

  inline fun findOrCompute(inPlaceUpdateOp: ((Int)->Unit)): Map<K, V> {
	return if (!cache.enableCache) {
	  compute(inPlaceUpdateOp)
	} else run {
	  cache[this] ?: compute(inPlaceUpdateOp).also {
		if (!cache.full) {
		  cache.full = !cache.setIfNotFull(this, it)
		}
	  }
	}
  }
}


abstract class GlobalRAMComputeInput<O>: ComputeInput<O>() {
  override val cacheManager: ComputeCacheManager get() = GlobalRAMComputeCacheManager
}

sealed class ComputeInput<O> {

  abstract val cacheManager: ComputeCacheManager

  @Suppress("UNCHECKED_CAST") @PublishedApi internal val cache: ComputeCacheBase<ComputeInput<O>, O> by lazy {
	cacheManager[this] as ComputeCacheBase<ComputeInput<O>, O>
  }


  abstract fun compute(): O
  operator fun invoke() = findOrCompute()
  internal open fun preFindOrCompute() {}
  fun findOrCompute(): O {
	preFindOrCompute()
	return if (!cache.enableCache) {
	  compute()
	} else run {
	  cache[this] ?: compute().also {
		if (!cache.full) {
		  cache.full = !cache.setIfNotFull(this, it)
		}
	  }
	}
  }
}

