package matt.caching.compcache.cache

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import matt.async.safe.mutSemMapOf
import matt.caching.compcache.ComputeInput
import matt.collect.map.lazyMutableMap
import matt.file.commons.CACHE_FOLDER
import kotlin.reflect.KClass

const val MAX_CACHE_SIZE = 1_000_000
private val COMP_CACHE_FOLDER = CACHE_FOLDER["compcache"]

class ComputeCache<I: ComputeInput<O>, O> private constructor(
  private val inputCls: KClass<ComputeInput<*>>,
  val enableCache: Boolean = true
) {
  var full = false
  val computeCache = mutSemMapOf<I, O>(maxsize = MAX_CACHE_SIZE)
  val cacheFile get() = COMP_CACHE_FOLDER["${inputCls.simpleName}.json"]

  operator fun get(input: I): O? = computeCache[input]
  operator fun set(input: I, output: O) {
	computeCache[input] = output
  }

  fun setIfNotFull(input: I, output: O) = computeCache.setIfNotFull(input, output)

  fun save() {
	val encoded = Json.encodeToString(computeCache)
	cacheFile.text = encoded
  }

  fun load() {
	val map = Json.decodeFromString<Map<I, O>>(cacheFile.text)
	computeCache.clear()
	computeCache.putAll(map)
  }

  internal var isSetup = false

  companion object {

	operator fun get(computeInput: ComputeInput<*>) = computeCaches[computeInput::class]!!

	@PublishedApi internal val computeCaches = lazyMutableMap<KClass<ComputeInput<*>>, ComputeCache<*, *>> { theClass ->
	  ComputeCache<ComputeInput<Any?>, Any?>(
		theClass
	  ).also {
		it.isSetup = true
	  }
	}

  }
}