package org.mechdancer.common

import org.mechdancer.algebra.function.vector.times
import org.mechdancer.geometry.angle.Angle
import org.mechdancer.geometry.angle.toVector

/** 极坐标 */
data class Polar(val distance: Double, val angle: Angle) {
    fun toVector2D() = angle.toVector() * distance
}
