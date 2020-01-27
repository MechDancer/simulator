package org.mechdancer.common

import org.mechdancer.algebra.implement.vector.Vector2D
import kotlin.math.cos
import kotlin.math.sin

/** 极坐标 */
data class Polar(val distance: Double, val angle: Double) {
    fun toVector2D() = Vector2D(distance * cos(angle), distance * sin(angle))
}
