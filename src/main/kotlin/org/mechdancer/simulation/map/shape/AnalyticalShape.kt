package org.mechdancer.simulation.map.shape

import org.mechdancer.algebra.implement.vector.Vector2D

interface AnalyticalShape : Shape {
    fun sample(): Sequence<Vector2D>
}
