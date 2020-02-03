package org.mechdancer.common.shape

import org.mechdancer.algebra.doubleEquals
import org.mechdancer.algebra.function.vector.dot
import org.mechdancer.algebra.function.vector.minus
import org.mechdancer.algebra.function.vector.plus
import org.mechdancer.algebra.function.vector.times
import org.mechdancer.algebra.implement.vector.Vector2D

/** 线段 */
data class Segment(
    val begin: Vector2D,
    val end: Vector2D
) {
    val connection get() = end - begin

    /** 判断线段相交 */
    infix fun intersect(others: Segment): Vector2D? {
        val a = this.connection
        val b = others.connection
        val (a0, _) = this
        val (b0, b1) = others
        return (a cross b)
            .takeUnless { doubleEquals(it, .0) }
            ?.let { ((b0 cross b) - (a0 cross b)) / it }
            ?.takeIf { it in 0.0..1.0 }
            ?.let { a0 + a * it }
            ?.takeIf { it - b0 dot it - b1 < 0 }
    }
}
