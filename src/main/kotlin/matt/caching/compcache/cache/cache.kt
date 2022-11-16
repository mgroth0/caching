package matt.caching.compcache.cache

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import matt.async.safe.mutSemMapOf
import matt.caching.compcache.ComputeInput
import matt.file.commons.CACHE_FOLDER
import kotlin.reflect.KClass

const val MAX_CACHE_SIZE = 1_000_000

class HardComputeCache<I: ComputeInput<O>, O>(
  inputCls: KClass<ComputeInput<*>>
): ComputeCache<I, O>(inputCls) {
  companion object {
	private val COMP_CACHE_FOLDER = CACHE_FOLDER["compcache"]
  }

  internal val cacheFile get() = COMP_CACHE_FOLDER["${inputCls.simpleName}.json"]

  fun save() {
	val encoded = Json.encodeToString(computeCache)
	cacheFile.text = encoded
  }

  fun load() {
	val map = Json.decodeFromString<Map<I, O>>(cacheFile.text)
	computeCache.clear()
	computeCache.putAll(map)
  }
}

open class ComputeCache<I: ComputeInput<O>, O>(
  protected val inputCls: KClass<ComputeInput<*>>,
  var enableCache: Boolean = true
) {
  var full = false
  val computeCache = mutSemMapOf<I, O>(maxsize = MAX_CACHE_SIZE)
  operator fun get(input: I): O? = computeCache[input]
  operator fun set(input: I, output: O) {
	computeCache[input] = output
  }

  fun setIfNotFull(input: I, output: O) = computeCache.setIfNotFull(input, output)
  internal var isSetup = false
}

