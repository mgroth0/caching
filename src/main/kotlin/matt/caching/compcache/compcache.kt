package matt.caching.compcache

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import matt.async.par.FutureMap
import matt.caching.compcache.cache.ComputeCache


abstract class ComputeInput<O> {

  @Suppress("UNCHECKED_CAST")
  @PublishedApi
  internal val cache by lazy { ComputeCache[this] as ComputeCache<ComputeInput<O>, O> }


  private val cacheFile by lazy { cache.cacheFile }

  private fun loadCache(): ComputeCache<ComputeInput<O>, O> {
	val s = cacheFile.text
	return Json.decodeFromString(s)
  }

  fun maybeLoad() {
	if (!cache.isSetup && cacheFile.exists() && !cacheFile.isBlank()) {
	  cache.load()
	  cache.isSetup = true
	}
  }


  abstract fun compute(): O
  operator fun invoke() = findOrCompute()

  fun findOrCompute(): O {
	maybeLoad()
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

abstract class UpdaterComputeInput<K, V>: ComputeInput<Map<K, V>>() {
  abstract fun futureMapBuilder(): FutureMap<K, V>
  override fun compute() = compute { }
  inline fun compute(op: (Int)->Unit): Map<K, V> {
	val fm = futureMapBuilder()
	fm.fill(op)
	return fm.map
  }

  operator fun invoke(inPlaceUpdateOp: ((Int)->Unit)) = findOrCompute(inPlaceUpdateOp)

  inline fun findOrCompute(inPlaceUpdateOp: ((Int)->Unit)): Map<K, V> {
	maybeLoad()
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