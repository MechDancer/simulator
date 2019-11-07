package org.mechdancer.common.shape

import org.mechdancer.algebra.function.vector.euclid
import org.mechdancer.algebra.implement.vector.Vector2D
import org.mechdancer.algebra.implement.vector.vector2DOf
import kotlin.math.*

class Ellipse(val a: Double, val b: Double, val sampleCount: Int = 16) : AnalyticalShape {
    val c = sqrt(abs(a * a - b * b))
    val d = hypot(min(a, b), c)
    override val size = PI * a * b
    override fun contains(p: Vector2D) =
        if (a > b) {
            (p euclid vector2DOf(+c, 0)) + (p euclid vector2DOf(-c, 0))
        } else {
            (p euclid vector2DOf(0,
                                 +c)) + (p euclid vector2DOf(0, -c))
        } < 2 * d

    override fun sample(): Polygon {
//        // 步进角
//        val theta = 2 * PI / sampleCount
//        // 等价半径
//        val equivalent = radius / sqrt(sin(theta) / theta)
//        // 生成
//        return List(sampleCount) { i -> (i * theta).toRad().toVector() * equivalent }.let(::Polygon)
        return TODO()
    }
}
