package org.mechdancer.common.shape

import org.mechdancer.simulation.Default.remote
import org.mechdancer.simulation.paint
import org.mechdancer.simulation.paintFrame2

fun main() {
    val ellipse = Ellipse(.4, 0.8)
    val x0 = -2 * (ellipse.a - ellipse.c)
    val x1 = 2 * ellipse.a
    val y0 = -1.2 * ellipse.b
    val y1 = -y0

    while (true) {
        remote.paint("椭圆", ellipse.sample())
        remote.paintFrame2("x 轴", listOf(x0 to .0, x1 to .0))
        remote.paintFrame2("y 轴", listOf(.0 to y0, .0 to y1))
        Thread.sleep(500L)
    }
}
