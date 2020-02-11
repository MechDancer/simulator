package org.mechdancer.common.shape

import org.mechdancer.algebra.implement.vector.vector2D
import org.mechdancer.simulation.Default.remote
import org.mechdancer.simulation.paint
import org.mechdancer.simulation.paint2DFrame

fun main() {
    val ellipse = Ellipse(.4, 0.8)
    val x0 = -2 * (ellipse.a - ellipse.c)
    val x1 = 2 * ellipse.a
    val y0 = -1.2 * ellipse.b
    val y1 = -y0

    while (true) {
        remote.paint("椭圆", ellipse.sample())
        remote.paint2DFrame("x 轴", listOf(vector2D(x0, 0), vector2D(x1, 0)))
        remote.paint2DFrame("y 轴", listOf(vector2D(0, y0), vector2D(0, y1)))
        Thread.sleep(500L)
    }
}
