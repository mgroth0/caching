package matt.caching.compcache.reporter

import matt.async.schedule.every
import matt.caching.compcache.GlobalRAMComputeInput
import matt.caching.compcache.cache.ComputeCache
import matt.log.tab
import matt.model.obj.single.SingleCall
import matt.prim.str.addSpacesUntilLengthIs
import matt.prim.str.truncate
import matt.reflect.reflections.mattSubClasses
import matt.time.dur.sec
import kotlin.reflect.full.companionObjectInstance

object ComputeInputReporter {
  val start = SingleCall {
	every(5.sec) {
	  println("ComputeCache Report")
	  tab("Name\t\tSize\t\tFull")
	  GlobalRAMComputeInput::class.mattSubClasses().forEach {
		val cache = (it.companionObjectInstance as ComputeCache<*, *>)
		val s = if (cache.enableCache) cache.computeCache.size else "DISABLED"
		tab(
		  "${it.simpleName!!.addSpacesUntilLengthIs(30).truncate(30)}\t\t${s}\t\t${cache.full}"
		)
	  }
	}
  }
}