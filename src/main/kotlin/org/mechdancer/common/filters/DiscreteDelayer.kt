package org.mechdancer.common.filters

import org.mechdancer.common.Stamped
import org.mechdancer.common.Stamped.Companion.stamp
import java.util.*

/** 离散延时器 */
class DiscreteDelayer<T : Any>(val size: Long) : Filter<T, Stamped<T>?> {
    private val queue: Queue<Stamped<T>> = LinkedList<Stamped<T>>()

    override fun update(new: T, time: Long?): Stamped<T>? {
        queue.offer(time?.let { Stamped(it, new) } ?: stamp(new))
        return if (queue.size > size) queue.poll() else null
    }

    override fun clear() {
        queue.clear()
    }

    companion object {
        fun <T : Any> delayOn(vararg list: T) =
            DiscreteDelayer<T>(list.size.toLong()).apply {
                val now = System.currentTimeMillis()
                for (item in list) update(item, now)
            }
    }
}
