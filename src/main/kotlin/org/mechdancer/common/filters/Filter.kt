package org.mechdancer.common.filters

/** 滤波器 */
interface Filter<T, R> {
    fun update(new: T, time: Long? = null): R
    fun clear()
}
