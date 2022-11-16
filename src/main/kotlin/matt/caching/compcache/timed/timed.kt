package matt.caching.compcache.timed

import matt.caching.compcache.GlobalRAMComputeInput
import matt.log.profile.stopwatch.Stopwatch
import matt.log.profile.stopwatch.stopwatch

abstract class TimedComputeInput<O>: GlobalRAMComputeInput<O>() {
  private var theStopwatch: Stopwatch? = null
  protected open val stopwatchEnabled = true
  final override fun compute(): O = stopwatch(this::class.simpleName!!,enabled = stopwatchEnabled) {
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