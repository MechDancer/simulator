package org.mechdancer.common.shape

/** 解析形状 */
interface AnalyticalShape : Shape {
    /** 采样（到多边形） */
    fun sample(): Polygon
}
