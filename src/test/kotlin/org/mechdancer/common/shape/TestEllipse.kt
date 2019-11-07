package org.mechdancer.common.shape

import org.mechdancer.simulation.Default.remote
import org.mechdancer.simulation.paint

fun main() {
    val ellipse = Ellipse(.4, 0.8)
    while (true) {
        remote.paint("椭圆", ellipse.sample())
        Thread.sleep(500L)
    }
}
