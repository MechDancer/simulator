package org.mechdancer.common.shape

interface AnalyticalShape : Shape {
    fun sample(): Polygon
}
