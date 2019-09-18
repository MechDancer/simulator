package org.mechdancer.common.filters

interface Filter<T, R> {
    fun update(new: T, time: Long? = null): R
    fun clear()
}
