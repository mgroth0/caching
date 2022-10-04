package matt.caching.compcache.timed

import matt.caching.compcache.ComputeInput
import matt.log.profile.Stopwatch
import matt.log.profile.stopwatch

abstract class TimedComputeInput<O>: ComputeInput<O>() {
  private var theStopwatch: Stopwatch? = null
  final override fun compute(): O = stopwatch(this::class.simpleName!!) {
	theStopwatch = this
	val r = timedCompute()
	theStopwatch = null
	r
  }

  protected fun toc(a: Any) {
	theStopwatch!!.toc(a)
  }

  abstract fun timedCompute(): O
}