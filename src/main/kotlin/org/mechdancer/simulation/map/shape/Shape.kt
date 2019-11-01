package org.mechdancer.simulation.map.shape

import org.mechdancer.algebra.implement.vector.Vector2D

/** 形状 */
interface Shape {
    val size: Double
    operator fun contains(p: Vector2D): Boolean
}
