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
  override var enableCache: Boolean = true
): ComputeCacheBase<I, O>() {
  val computeCache = mutSemMapOf<I, O>(maxsize = MAX_CACHE_SIZE)
  override operator fun get(input: I): O? = computeCache[input]
  override operator fun set(input: I, output: O) {
	computeCache[input] = output
  }

  override fun setIfNotFull(input: I, output: O) = computeCache.setIfNotFull(input, output)
}


class FakeComputeCache<I: ComputeInput<O>, O>(): ComputeCacheBase<I, O>() {
  override fun get(input: I) = null
  override fun set(input: I, output: O) = Unit
  override var enableCache = true
  override fun setIfNotFull(input: I, output: O) = false
}


abstract class ComputeCacheBase<I: ComputeInput<O>, O>() {
  abstract var enableCache: Boolean
  abstract operator fun get(input: I): O?
  abstract operator fun set(input: I, output: O)
  var full = false
  abstract fun setIfNotFull(input: I, output: O): Boolean
  internal var isSetup = false
}