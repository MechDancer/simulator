package org.mechdancer.simulation

import org.mechdancer.algebra.implement.vector.vector2DOf
import org.mechdancer.simulation.map.loadAsScan
import org.mechdancer.simulation.map.saveToBmp
import java.io.File

fun main() {
    listOf(vector2DOf(0, 0),
           vector2DOf(1, 1),
           vector2DOf(2, 2)
    ).saveToBmp("test")
    File("test.bmp")
        .loadAsScan()
        .joinToString("\n") { "${it.x} ${it.y}" }
        .let(::println)
}
