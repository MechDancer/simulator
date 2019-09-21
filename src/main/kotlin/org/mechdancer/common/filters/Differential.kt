package org.mechdancer.common.filters

import org.mechdancer.common.Stamped

/** 差分器 */
class Differential<T : Any, R>(
    private val init: T,
    private val t0: Long? = null,
    private val minus: (dt: Long, old: T, new: T) -> R
) : Filter<T, Stamped<R>> {
    private val delayer = DiscreteDelayer<T>(1)

    init {
        clear()
    }

    override fun update(new: T, time: Long?): Stamped<R> {
        val t1 = time ?: System.currentTimeMillis()
        val (t0, old) = delayer.update(new, t1)!!
        return Stamped(t0, minus(t1 - t0, old, new))
    }

    override fun clear() {
        delayer.update(init, t0)
    }
}
