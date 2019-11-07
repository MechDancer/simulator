package org.mechdancer.common.shape

import org.mechdancer.algebra.function.vector.norm
import org.mechdancer.algebra.implement.vector.Vector2D
import org.mechdancer.common.Polar
import org.mechdancer.geometry.angle.toAngle
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sqrt

class Ellipse(
    val c: Double,
    val e: Double,
    val sampleCount: Int = 64
) : AnalyticalShape {
    init {
        require(c > 0)
        require(0 < e && e < 1)
    }

    val a = c / e
    val b = sqrt(a * a - c * c)
    val p = b * b / c

    override val size = PI * a * b
    override fun contains(p: Vector2D) = p.norm() < rho(p.toAngle().asRadian())

    override fun sample(): Polygon {
        // 步进角
        val step = 2 * PI / sampleCount
        // 生成
        return List(sampleCount) { i ->
            (i * step).let { theta -> Polar(rho(theta), theta) }.toVector2D()
        }.let(::Polygon)
    }

    private fun rho(theta: Double) = e * p / (1 - e * cos(theta))

}
