package org.mechdancer.simulation.map.shape

import org.mechdancer.algebra.implement.vector.Vector2D

internal infix fun Vector2D.cross(others: Vector2D): Double {
    val (x0, y0) = this
    val (x1, y1) = others
    return x0 * y1 - x1 * y0
}

internal operator fun Vector2D.rangeTo(others: Vector2D) =
    Segment(this, others)
