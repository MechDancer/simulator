package org.mechdancer.common

/** 带时间戳 [time] 的 [T] 类型数据 */
data class Stamped<T>(
    val time: Long,
    val data: T
) : Comparable<Stamped<*>> {
    override fun compareTo(other: Stamped<*>) = time.compareTo(other.time)

    companion object {
        fun <T> stamp(data: T) = Stamped(System.currentTimeMillis(), data)
    }
}
